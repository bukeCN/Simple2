package com.live.simple2.proformance.trace;

import com.live.simple2.proformance.ComponentObserver;
import com.live.simple2.proformance.FrameDataItem;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * UI 监控数据处理
 */
public class UITraceControl {
    // 一次数据合并节点，不做实时上报
    private static final int onceDataCountLimit = 200;

    private LinkedList<FrameDataItem> frameDataItemList;

    private ComponentObserver componentObserver;

    private FrameFPSControl fpsControl;

    public UITraceControl(ComponentObserver componentObserver) {
        frameDataItemList = new LinkedList<>();
        this.componentObserver = componentObserver;

        fpsControl = new FrameFPSControl();
    }

    public void collectData(String topActivity, long startNas, long endNas, boolean isVsync, long frameIntervalNanos) {
        if (frameDataItemList.size()  >0 && checkLastItem(topActivity)){
            // activity 不同，上报上一批数据
            List<FrameDataItem> copyList = new LinkedList<>(frameDataItemList);
            // 准备后面进行上传。
            frameDataItemList.clear();
        }

        FrameDataItem item = new FrameDataItem();
        item.activity = topActivity;
        frameDataItemList.add(item);

        if (frameDataItemList.size() >= onceDataCountLimit) {
            // 处理数据，准备上报

        }
    }


    /**
     * 检查上一个 item 是否和当前这个同属于一个 activity 页面
     *
     * @return
     */
    private boolean checkLastItem(String activity) {
        boolean result = true;
        if (frameDataItemList.getLast().activity.equals(activity)) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    // 数据预处理，子线程完成，调用上报
    private class FrameFPSControl implements Runnable{
        Executor executor;

        public FrameFPSControl(){

        }

        @Override
        public void run() {

        }
    }

}
