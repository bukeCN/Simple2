package com.live.simple2.proformance.compoent;

import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;

import com.live.simple2.proformance.Component;
import com.live.simple2.proformance.LooperMonitor;
import com.live.simple2.proformance.LooperObserver;
import com.live.simple2.proformance.UITraceControl;
import com.live.simple2.proformance.utils.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * UI 线程监控组件
 */
public class UIThreadComponent implements Component, LooperObserver, Runnable {

    private UITraceControl uiTraceControl;

    private Choreographer choreographer;

    private Object[] callbackQueues;

    private Method addInputQueue;

    // 消息开始分发时间
    private long token;
    // 是否是 vsync 信号触发的消息
    private boolean isVsync = false;
    // 预期 vsync 间隔，纳秒
    private long frameIntervalNanos;


    @Override
    public void init() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            return;
        }

        uiTraceControl = new UITraceControl();

        choreographer = Choreographer.getInstance();

        // 通过反射获取 choreographer 中的 vsync 回调对象，后面用它在头部添加 vsync 回调。
        callbackQueues = ReflectUtils.reflectObject(choreographer, "mCallbackQueues", null);
        // 反射获取向队列中获取回调的方法, 0 表示 INPUT 类型回调
        addInputQueue = ReflectUtils.reflectMethod(callbackQueues[0], "addCallbackLocked", long.class, Object.class, Object.class);
        // 获取正常的预期的 VSYNC 信号回调处理时间间隔，根据这个可以判断在处理某一 VSYNC 信号时长是否大于预期间隔，如果大于则认为发生了卡顿
        frameIntervalNanos = ReflectUtils.reflectObject(choreographer, "mFrameIntervalNanos", 16666667L);
    }

    @Override
    public void startTrace() {
        LooperMonitor.registObserver(this);
    }

    @Override
    public void stopTrace() {
        LooperMonitor.unRegistObserver(this);
    }

    @Override
    public void dispatchStart() {
        // 记录当前时间，纳秒
        token = System.nanoTime();
        // 向 vsync 信号回调头部插入回调
        try {
            addInputQueue.invoke(0, true, this, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispatchEnd() {
        // 开始处理的纳秒
        long startNas = token;
        // 结束时的纳秒
        long endNas = System.nanoTime();
        // 统计数据
        collectData(startNas,endNas,isVsync,frameIntervalNanos);

        isVsync = false;
    }

    /**
     *
     * @param startNas 开始时间，纳秒
     * @param endNas 结束时间，纳秒
     * @param isVsync 是否是 vsync 信号触发
     * @param frameIntervalNanos 预期完成时间
     */
    private void collectData(long startNas, long endNas, boolean isVsync, long frameIntervalNanos) {
        uiTraceControl.collectData("",startNas,endNas,isVsync,frameIntervalNanos);
    }

    @Override
    public void run() {
        // 改变标志
        isVsync = true;
    }
}
