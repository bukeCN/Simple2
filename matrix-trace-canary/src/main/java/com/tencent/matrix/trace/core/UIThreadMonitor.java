package com.tencent.matrix.trace.core;

import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;

import com.tencent.matrix.trace.config.TraceConfig;
import com.tencent.matrix.trace.constants.Constants;
import com.tencent.matrix.trace.listeners.LooperObserver;
import com.tencent.matrix.trace.util.Utils;
import com.tencent.matrix.util.MatrixLog;
import com.tencent.matrix.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

public class UIThreadMonitor implements BeatLifecycle, Runnable {

    private static final String TAG = "Matrix.UIThreadMonitor";
    private static final String ADD_CALLBACK = "addCallbackLocked";
    private volatile boolean isAlive = false;
    private long[] dispatchTimeMs = new long[4];
    private final HashSet<LooperObserver> observers = new HashSet<>();
    private volatile long token = 0L;
    private boolean isVsyncFrame = false;
    // The time of the oldest input event
    private static final int OLDEST_INPUT_EVENT = 3;

    // The time of the newest input event
    private static final int NEWEST_INPUT_EVENT = 4;

    /**
     * Callback type: Input callback.  Runs first.
     *
     * @hide
     */
    public static final int CALLBACK_INPUT = 0;

    /**
     * Callback type: Animation callback.  Runs before traversals.
     *
     * @hide
     */
    public static final int CALLBACK_ANIMATION = 1;

    /**
     * Callback type: Commit callback.  Handles post-draw operations for the frame.
     * Runs after traversal completes.
     *
     * @hide
     */
    public static final int CALLBACK_TRAVERSAL = 2;

    /**
     * never do queue end code
     */
    public static final int DO_QUEUE_END_ERROR = -100;

    private static final int CALLBACK_LAST = CALLBACK_TRAVERSAL;

    private final static UIThreadMonitor sInstance = new UIThreadMonitor();
    private TraceConfig config;
    private Object callbackQueueLock;
    private Object[] callbackQueues;
    private Method addTraversalQueue;
    private Method addInputQueue;
    private Method addAnimationQueue;
    private Choreographer choreographer;
    private Object vsyncReceiver;
    private long frameIntervalNanos = 16666666;
    private int[] queueStatus = new int[CALLBACK_LAST + 1];
    private boolean[] callbackExist = new boolean[CALLBACK_LAST + 1]; // ABA
    private long[] queueCost = new long[CALLBACK_LAST + 1];
    private static final int DO_QUEUE_DEFAULT = 0;
    private static final int DO_QUEUE_BEGIN = 1;
    private static final int DO_QUEUE_END = 2;
    private boolean isInit = false;

    public static UIThreadMonitor getMonitor() {
        return sInstance;
    }

    public boolean isInit() {
        return isInit;
    }

    // UIThreadMonitor 初始化
    public void init(TraceConfig config) {
        // 必须执行在主线程
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new AssertionError("must be init in main thread!");
        }
        this.config = config;
        // 获取线程单例的 Choreographer
        choreographer = Choreographer.getInstance();
        // 通过反射获取 Choreographer 的锁对象
        callbackQueueLock = ReflectUtils.reflectObject(choreographer, "mLock", new Object());
        // 通过反射获取 Choreographer 的 CallbackQueue，这个里面保存了 VSYNC 信号到来时需要进行调用的任务。
        callbackQueues = ReflectUtils.reflectObject(choreographer, "mCallbackQueues", null);

        if (null != callbackQueues) {
            // 通过反射获取添加 input 类型的回调方法以备调用
            addInputQueue = ReflectUtils.reflectMethod(callbackQueues[CALLBACK_INPUT], ADD_CALLBACK, long.class, Object.class, Object.class);
            // 通过反射获取添加 animation 类型的回调方法以备调用
            addAnimationQueue = ReflectUtils.reflectMethod(callbackQueues[CALLBACK_ANIMATION], ADD_CALLBACK, long.class, Object.class, Object.class);
            // 通过反射获取添加 ui 绘制类型的方法以备调用
            addTraversalQueue = ReflectUtils.reflectMethod(callbackQueues[CALLBACK_TRAVERSAL], ADD_CALLBACK, long.class, Object.class, Object.class);
        }
        // 获取 VSYNC 信号监听
        vsyncReceiver = ReflectUtils.reflectObject(choreographer, "mDisplayEventReceiver", null);
        // 获取正常的预期的 VSYNC 信号回调处理时间间隔，根据这个可以判断在处理某一 VSYNC 信号时长是否大于预期间隔，如果大于则认为发生了卡顿
        frameIntervalNanos = ReflectUtils.reflectObject(choreographer, "mFrameIntervalNanos", Constants.DEFAULT_FRAME_DURATION);
        // 监听主线程 Looper 处理 message 前后，start() 对应处理消息之前，end() 对应消息处理完毕。
        // 为什么这里需要监听呢？
        LooperMonitor.register(new LooperMonitor.LooperDispatchListener() {
            @Override
            public boolean isValid() {
                return isAlive;
            }

            @Override
            public void dispatchStart() {
                super.dispatchStart();
                UIThreadMonitor.this.dispatchBegin();
            }

            @Override
            public void dispatchEnd() {
                super.dispatchEnd();
                UIThreadMonitor.this.dispatchEnd();
            }

        });
        this.isInit = true;
        MatrixLog.i(TAG, "[UIThreadMonitor] %s %s %s %s %s %s frameIntervalNanos:%s", callbackQueueLock == null, callbackQueues == null,
                addInputQueue == null, addTraversalQueue == null, addAnimationQueue == null, vsyncReceiver == null, frameIntervalNanos);

        // 开发时输出
        if (config.isDevEnv()) {
            addObserver(new LooperObserver() {
                @Override
                public void doFrame(String focusedActivity, long startNs, long endNs, boolean isVsyncFrame, long intendedFrameTimeNs, long inputCostNs, long animationCostNs, long traversalCostNs) {
                    MatrixLog.i(TAG, "focusedActivity[%s] frame cost:%sms isVsyncFrame=%s intendedFrameTimeNs=%s [%s|%s|%s]ns",
                            focusedActivity, (endNs - startNs) / Constants.TIME_MILLIS_TO_NANO, isVsyncFrame, intendedFrameTimeNs, inputCostNs, animationCostNs, traversalCostNs);
                }
            });
        }
    }

    private synchronized void addFrameCallback(int type, Runnable callback, boolean isAddHeader) {
        if (callbackExist[type]) {
            MatrixLog.w(TAG, "[addFrameCallback] this type %s callback has exist! isAddHeader:%s", type, isAddHeader);
            return;
        }

        if (!isAlive && type == CALLBACK_INPUT) {
            MatrixLog.w(TAG, "[addFrameCallback] UIThreadMonitor is not alive!");
            return;
        }
        try {
            // callbackQueueLock 加锁，判断 type 调用不同的添加 FrameCallback 回调方法。
            synchronized (callbackQueueLock) {
                Method method = null;
                switch (type) {
                    case CALLBACK_INPUT:
                        method = addInputQueue;
                        break;
                    case CALLBACK_ANIMATION:
                        method = addAnimationQueue;
                        break;
                    case CALLBACK_TRAVERSAL:
                        method = addTraversalQueue;
                        break;
                }
                if (null != method) {
                    // 通过反射调用，监听中添加回调方法，isAddHeader 代表是否向 Chorgerapher 回调队列的头部添加，这样就能保证该 Callback 在系统添加的前被调用
                    method.invoke(callbackQueues[type], !isAddHeader ? SystemClock.uptimeMillis() : -1, callback, null);
                    callbackExist[type] = true;
                }
            }
        } catch (Exception e) {
            MatrixLog.e(TAG, e.toString());
        }
    }

    public long getFrameIntervalNanos() {
        return frameIntervalNanos;
    }

    public void addObserver(LooperObserver observer) {
        if (!isAlive) {
            onStart();
        }
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(LooperObserver observer) {
        synchronized (observers) {
            observers.remove(observer);
            if (observers.isEmpty()) {
                onStop();
            }
        }
    }

    // 一个 Handler 中一个消息执行前回调
    private void dispatchBegin() {
        // 获取当前系统时间，纳秒！
        token = dispatchTimeMs[0] = System.nanoTime();
        // 获取当前线程运行时间,毫秒!
        dispatchTimeMs[2] = SystemClock.currentThreadTimeMillis();
        // ？？统计消息耗时的方法？
        AppMethodBeat.i(AppMethodBeat.METHOD_ID_DISPATCH);
        // 回调方法
        synchronized (observers) {
            for (LooperObserver observer : observers) {
                if (!observer.isDispatchBegin()) {
                    // 回调数据收集开始，实现看 FrameTracer 中
                    // 参数为，当前系统纳秒级时间、当前线程运行时间、token（当前系统纳秒级时间）
                    observer.dispatchBegin(dispatchTimeMs[0], dispatchTimeMs[2], token);
                }
            }
        }
        if (config.isDevEnv()) {
            MatrixLog.d(TAG, "[dispatchBegin#run] inner cost:%sns", System.nanoTime() - token);
        }
    }

    // 是否是 vsync 信号
    private void doFrameBegin(long token) {
        this.isVsyncFrame = true;
    }

    private void doFrameEnd(long token) {
        // 同开始，TRAVERSAL 事件结束，改变状态，记录数据
        doQueueEnd(CALLBACK_TRAVERSAL);

        for (int i : queueStatus) {
            if (i != DO_QUEUE_END) {
                queueCost[i] = DO_QUEUE_END_ERROR;
                if (config.isDevEnv) {
                    throw new RuntimeException(String.format("UIThreadMonitor happens type[%s] != DO_QUEUE_END", i));
                }
            }
        }
        queueStatus = new int[CALLBACK_LAST + 1];
        // 再向 Choreographer 的 vsync 信号回调队列头部插入回调，准备下一次回调监听。
        addFrameCallback(CALLBACK_INPUT, this, true);

    }

    // Message 执行完毕
    private void dispatchEnd() {
        long traceBegin = 0;
        if (config.isDevEnv()) {
            traceBegin = System.nanoTime();
        }
        // 开始的时间，纳秒，在 dispatchBegin 中获取
        long startNs = token;
        long intendedFrameTimeNs = startNs;
        // 是否是 VSYNC 同步信号
        if (isVsyncFrame) {
            // VYNC 信号执行结束调用
            doFrameEnd(token);
            // 获取 vsync 到来时候的时间戳，纳秒
            intendedFrameTimeNs = getIntendedFrameTimeNs(startNs);
        }
        // 结束时的纳秒
        long endNs = System.nanoTime();
        // 回调监听
        synchronized (observers) {
            for (LooperObserver observer : observers) {
                if (observer.isDispatchBegin()) {
                    observer.doFrame(AppMethodBeat.getVisibleScene(), startNs, endNs, isVsyncFrame, intendedFrameTimeNs,
                            queueCost[CALLBACK_INPUT], queueCost[CALLBACK_ANIMATION], queueCost[CALLBACK_TRAVERSAL]);
                }
            }
        }
        // 记录当前线程时间和系统时间纳秒
        dispatchTimeMs[3] = SystemClock.currentThreadTimeMillis();
        dispatchTimeMs[1] = System.nanoTime();

        AppMethodBeat.o(AppMethodBeat.METHOD_ID_DISPATCH);
        // 又一次回调
        synchronized (observers) {
            for (LooperObserver observer : observers) {
                if (observer.isDispatchBegin()) {
                    observer.dispatchEnd(dispatchTimeMs[0], dispatchTimeMs[2], dispatchTimeMs[1], dispatchTimeMs[3], token, isVsyncFrame);
                }
            }
        }
        this.isVsyncFrame = false;

        if (config.isDevEnv()) {
            MatrixLog.d(TAG, "[dispatchEnd#run] inner cost:%sns", System.nanoTime() - traceBegin);
        }
    }

    private void doQueueBegin(int type) {
        // 利用数组保存状态，type 对应 0,1,2  和 INPUT / ANIMAN / TRAVERSAL
        queueStatus[type] = DO_QUEUE_BEGIN;
        // 记录每一个时间开始执行的时间
        queueCost[type] = System.nanoTime();
    }

    private void doQueueEnd(int type) {
        // 记录某一事件的状态
        queueStatus[type] = DO_QUEUE_END;
        // 记录某一事件执行的耗时
        queueCost[type] = System.nanoTime() - queueCost[type];
        synchronized (this) {
            callbackExist[type] = false;
        }
    }

    // 启动 UIThreadMonitor 监控
    @Override
    public synchronized void onStart() {
        if (!isInit) {
            MatrixLog.e(TAG, "[onStart] is never init.");
            return;
        }
        if (!isAlive) {
            this.isAlive = true;
            // ？？？？解决多线程 ABA 问题？？？
            synchronized (this) {
                MatrixLog.i(TAG, "[onStart] callbackExist:%s %s", Arrays.toString(callbackExist), Utils.getStack());
                callbackExist = new boolean[CALLBACK_LAST + 1];
            }
            queueStatus = new int[CALLBACK_LAST + 1];
            queueCost = new long[CALLBACK_LAST + 1];
            // 添加 FrameCallback，监听 VSYNC 信号回调
            addFrameCallback(CALLBACK_INPUT, this, true);
        }
    }

    @Override
    public void run() {
        // 获取开始时间
        final long start = System.nanoTime();
        try {
            // token 在 Message 执行前被回调，token 为当前系统时间，纳秒。
            doFrameBegin(token);
            // vsync 信号回调队列开始执行，首先是记录 INPUT  类型开始执行
            doQueueBegin(CALLBACK_INPUT);
            // 添加 ANIMATION 类型回调，记录 ANIMATION 回调处理时间
            addFrameCallback(CALLBACK_ANIMATION, new Runnable() {

                @Override
                public void run() {
                    // 执行完毕，保存事件执行数据(耗时)，改变事件执行状态为结束
                    doQueueEnd(CALLBACK_INPUT);
                    // 改变下一事件 ANIMATION 的状态
                    doQueueBegin(CALLBACK_ANIMATION);
                }
            }, true);
            // 添加 TRAVERSAL 类型回调，记录 TRAVERSAL 回调处理时间
            addFrameCallback(CALLBACK_TRAVERSAL, new Runnable() {

                @Override
                public void run() {
                    // 同 ANIMATION 事件
                    doQueueEnd(CALLBACK_ANIMATION);
                    doQueueBegin(CALLBACK_TRAVERSAL);
                }
            }, true);

        } finally {
            if (config.isDevEnv()) {
                MatrixLog.d(TAG, "[UIThreadMonitor#run] inner cost:%sns", System.nanoTime() - start);
            }
        }
    }


    @Override
    public synchronized void onStop() {
        if (!isInit) {
            MatrixLog.e(TAG, "[onStart] is never init.");
            return;
        }
        if (isAlive) {
            this.isAlive = false;
            MatrixLog.i(TAG, "[onStop] callbackExist:%s %s", Arrays.toString(callbackExist), Utils.getStack());
        }
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    private long getIntendedFrameTimeNs(long defaultValue) {
        try {
            return ReflectUtils.reflectObject(vsyncReceiver, "mTimestampNanos", defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            MatrixLog.e(TAG, e.toString());
        }
        return defaultValue;
    }

    public long getQueueCost(int type, long token) {
        if (token != this.token) {
            return -1;
        }
        return queueStatus[type] == DO_QUEUE_END ? queueCost[type] : 0;
    }

    private long[] frameInfo = null;

    public long getInputEventCost() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Object obj = ReflectUtils.reflectObject(choreographer, "mFrameInfo", null);
            if (null == frameInfo) {
                frameInfo = ReflectUtils.reflectObject(obj, "frameInfo", null);
                if (null == frameInfo) {
                    frameInfo = ReflectUtils.reflectObject(obj, "mFrameInfo", new long[9]);
                }
            }
            long start = frameInfo[OLDEST_INPUT_EVENT];
            long end = frameInfo[NEWEST_INPUT_EVENT];
            return end - start;
        }
        return 0;
    }
}
