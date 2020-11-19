package com.tencent.matrix.trace.listeners;

import android.support.annotation.CallSuper;

public abstract class LooperObserver {

    private boolean isDispatchBegin = false;


    @CallSuper
    public void dispatchBegin(long beginNs, long cpuBeginNs, long token) {
        // UIThreadMontior 的回调仅仅只是将回调其置位 true。
        isDispatchBegin = true;
    }

    public void doFrame(String focusedActivity, long startNs, long endNs, boolean isVsyncFrame, long intendedFrameTimeNs, long inputCostNs, long animationCostNs, long traversalCostNs) {

    }

    @CallSuper
    public void dispatchEnd(long beginNs, long cpuBeginMs, long endNs, long cpuEndMs, long token, boolean isVsyncFrame) {
        // 置为 false
        isDispatchBegin = false;
    }

    public boolean isDispatchBegin() {
        return isDispatchBegin;
    }
}
