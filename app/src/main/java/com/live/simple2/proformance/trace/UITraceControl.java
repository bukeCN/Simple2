package com.live.simple2.proformance.trace;

import com.live.simple2.proformance.ComponentObserver;
import com.live.simple2.proformance.data.FrameDataItem;
import com.live.simple2.proformance.data.FrameFPSControl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * UI 监控数据处理
 */
public class UITraceControl {
    // 一次数据合并节点，不做实时上报
    private static final int onceDataCountLimit = 200;

    private LinkedList<FrameDataItem> frameDataItemList;

    private ComponentObserver componentObserver;

    private Executor executor;

    private FrameFPSControl frameFPSControl;

    public UITraceControl(ComponentObserver componentObserver) {
        frameDataItemList = new LinkedList<>();
        this.componentObserver = componentObserver;

        frameFPSControl = new FrameFPSControl(componentObserver);
    }

    /**
     *
     * @param topActivity 当前 activity
     * @param startNas 开始时间纳秒
     * @param endNas 结束时间纳秒
     * @param dropFrame 该次 handler 消息处理掉帧数，
     * @param isVsync 是否是由 vsync 信号触发此次消息
     * @param frameIntervalNanos 预期最佳处理消息时间间隔纳秒
     */
    public void collectData(String topActivity, long startNas, long endNas, int dropFrame , boolean isVsync, long frameIntervalNanos) {
        // 记录单条数据
        FrameDataItem item = new FrameDataItem();
        item.activity = topActivity;
        item.startNs = startNas;
        item.endNs = endNas;
        item.dropFrame = dropFrame;
        item.isVsyncFrame = isVsync;
        item.intendedFrameTimeNs = frameIntervalNanos;
        frameDataItemList.add(item);
        // 是否达到上报限制，达到即上报
        if (frameDataItemList.size() >= onceDataCountLimit) {
            uploadData();
        }
    }


    private void uploadData() {
        // activity 不同，上报上一批数据
        List<FrameDataItem> copyList = new LinkedList<>(frameDataItemList);
        // 准备后面进行上传。
        frameDataItemList.clear();

        executeTask(copyList);
    }

    private void executeTask(List<FrameDataItem> copyList) {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                frameFPSControl.doRecollect(copyList);
            }
        });
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


    private Executor getExecutor(){
        if (executor == null){
            executor = Executors.newFixedThreadPool(4);
        }
        return executor;
    }

}
