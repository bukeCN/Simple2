package com.tencent.matrix.trace.core;

import android.os.Build;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.util.Log;
import android.util.Printer;

import com.tencent.matrix.util.MatrixLog;
import com.tencent.matrix.util.ReflectUtils;

import java.util.HashSet;
import java.util.Objects;

/**
 * 其内部进行了实例化，获取了主线程的 Looper
 */
public class LooperMonitor implements MessageQueue.IdleHandler {

    private final HashSet<LooperDispatchListener> listeners = new HashSet<>();
    private static final String TAG = "Matrix.LooperMonitor";
    private LooperPrinter printer;
    private Looper looper;
    private static final long CHECK_TIME = 60 * 1000L;
    private long lastCheckPrinterTime = 0;

    public abstract static class LooperDispatchListener {

        boolean isHasDispatchStart = false;

        public boolean isValid() {
            return false;
        }


        public void dispatchStart() {

        }

        @CallSuper
        public void onDispatchStart(String x) {
            this.isHasDispatchStart = true;
            dispatchStart();
        }

        @CallSuper
        public void onDispatchEnd(String x) {
            this.isHasDispatchStart = false;
            dispatchEnd();
        }


        public void dispatchEnd() {
        }
    }

    private static final LooperMonitor mainMonitor = new LooperMonitor();

    static void register(LooperDispatchListener listener) {
        mainMonitor.addListener(listener);
    }

    static void unregister(LooperDispatchListener listener) {
        mainMonitor.removeListener(listener);
    }

    public HashSet<LooperDispatchListener> getListeners() {
        return listeners;
    }

    public void addListener(LooperDispatchListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(LooperDispatchListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public LooperMonitor(Looper looper) {
        Objects.requireNonNull(looper);
        this.looper = looper;
        resetPrinter();
        addIdleHandler(looper);
    }

    private LooperMonitor() {
        this(Looper.getMainLooper());
    }

    @Override
    public boolean queueIdle() {
        if (SystemClock.uptimeMillis() - lastCheckPrinterTime >= CHECK_TIME) {
            resetPrinter();
            lastCheckPrinterTime = SystemClock.uptimeMillis();
        }
        return true;
    }

    public synchronized void onRelease() {
        if (printer != null) {
            synchronized (listeners) {
                listeners.clear();
            }
            MatrixLog.v(TAG, "[onRelease] %s, origin printer:%s", looper.getThread().getName(), printer.origin);
            looper.setMessageLogging(printer.origin);
            removeIdleHandler(looper);
            looper = null;
            printer = null;
        }
    }

    private static boolean isReflectLoggingError = false;

    private synchronized void resetPrinter() {
        Printer originPrinter = null;
        try {
            if (!isReflectLoggingError) {
                originPrinter = ReflectUtils.get(looper.getClass(), "mLogging", looper);
                if (originPrinter == printer && null != printer) {
                    return;
                }
            }
        } catch (Exception e) {
            isReflectLoggingError = true;
            Log.e(TAG, "[resetPrinter] %s", e);
        }

        if (null != printer) {
            MatrixLog.w(TAG, "maybe thread:%s printer[%s] was replace other[%s]!",
                    looper.getThread().getName(), printer, originPrinter);
        }
        // 向主线程的 Looper 中设置 Message 消息执行前后的监听。
        looper.setMessageLogging(printer = new LooperPrinter(originPrinter));
        if (null != originPrinter) {
            MatrixLog.i(TAG, "reset printer, originPrinter[%s] in %s", originPrinter, looper.getThread().getName());
        }
    }

    private synchronized void removeIdleHandler(Looper looper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            looper.getQueue().removeIdleHandler(this);
        } else {
            try {
                MessageQueue queue = ReflectUtils.get(looper.getClass(), "mQueue", looper);
                queue.removeIdleHandler(this);
            } catch (Exception e) {
                Log.e(TAG, "[removeIdleHandler] %s", e);
            }

        }
    }

    private synchronized void addIdleHandler(Looper looper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            looper.getQueue().addIdleHandler(this);
        } else {
            try {
                MessageQueue queue = ReflectUtils.get(looper.getClass(), "mQueue", looper);
                queue.addIdleHandler(this);
            } catch (Exception e) {
                Log.e(TAG, "[removeIdleHandler] %s", e);
            }
        }
    }


    class LooperPrinter implements Printer {
        public Printer origin;
        boolean isHasChecked = false;
        boolean isValid = false;

        LooperPrinter(Printer printer) {
            this.origin = printer;
        }

        // 输出消息方法
        @Override
        public void println(String x) {
            // origin 表示最源 Prointer ，即兼容系统添加的 Printer，别自己添加了又把系统的给干掉了。
            if (null != origin) {
                origin.println(x);
                if (origin == this) {
                    throw new RuntimeException(TAG + " origin == this");
                }
            }
            // 通过获取输出消息的第一个字符判断是 Message 执行前调用，还是执行后调用。输出信息格式在源码中有。
            if (!isHasChecked) {
                // 检查消息格式是否正确，万一 Google 抽风或者其他 OS 给改了
                isValid = x.charAt(0) == '>' || x.charAt(0) == '<';
                isHasChecked = true;
                if (!isValid) {
                    MatrixLog.e(TAG, "[println] Printer is inValid! x:%s", x);
                }
            }

            if (isValid) {
                // 调用分发
                dispatch(x.charAt(0) == '>', x);
            }

        }
    }


    private void dispatch(boolean isBegin, String log) {
        // 这里即会回调 UIThreadMonitor 注册的回调方法。
        for (LooperDispatchListener listener : listeners) {
            if (listener.isValid()) {
                if (isBegin) {
                    if (!listener.isHasDispatchStart) {
                        // 调用开始
                        listener.onDispatchStart(log);
                    }
                } else {
                    if (listener.isHasDispatchStart) {
                        // 调用结束
                        listener.onDispatchEnd(log);
                    }
                }
            } else if (!isBegin && listener.isHasDispatchStart) {
                listener.dispatchEnd();
            }
        }

    }


}
