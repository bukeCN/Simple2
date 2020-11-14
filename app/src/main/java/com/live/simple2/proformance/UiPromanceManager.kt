package com.live.simple2.proformance

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer

/**
 * 简易的 fps 监听
 */
class UiPromanceManager {
    // 获取 ui 线程 handler
    val mMainHandler = Handler(Looper.getMainLooper())
    // 实例化两个监控任务，runnable 用来显示 fps 值，framecallback 用于统计 vsync 信号
    var frameRateTask = FrameRateTask(mMainHandler)

    // 启动开始监控
    fun monitorFPS(){
        mMainHandler.postDelayed(frameRateTask,1000)
        Choreographer.getInstance().postFrameCallback(frameRateTask)
    }
    // 停止监控
    fun unMonitorFPS(){
        mMainHandler.removeCallbacks(frameRateTask)
        Choreographer.getInstance().removeFrameCallback(frameRateTask)
    }
}


class FrameRateTask(handler: Handler) : Runnable, Choreographer.FrameCallback{
    var fpsCount = 0

    var mHandler = handler

    override fun run() {
        // 显示 fps
        Log.e("sun","fps：" + fpsCount)
        fpsCount = 0;
        mHandler.postDelayed(this,1000)
    }

    override fun doFrame(frameTimeNanos: Long) {
        fpsCount++
        Choreographer.getInstance().postFrameCallback(this)
    }
}