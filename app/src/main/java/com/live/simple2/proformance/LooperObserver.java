package com.live.simple2.proformance;

/**
 * Looper 消息执行观察者
 */
public interface LooperObserver {

    /**
     * 消息开始分发执行
     */
    void dispatchStart();

    /**
     * 消息分发执行结束
     */
    void dispatchEnd();
}
