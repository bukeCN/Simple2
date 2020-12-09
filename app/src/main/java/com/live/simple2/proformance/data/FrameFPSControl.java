package com.live.simple2.proformance.data;

import com.live.simple2.proformance.ComponentObserver;
import com.live.simple2.proformance.Issue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * 最终合成数据，触发上报
 */
public class FrameFPSControl {
    private long uploadDataTimeLimit = 1000 * 6;// 需要记录的时间总和超过或达到该值触发上报

    private int frozen = 42;
    private int high = 24;
    private int middle = 24;
    private int normal = 9;
    private int best = 3;

    private ComponentObserver componentObserver;

    private HashMap<String, RealFrameFPSItem> map = new HashMap<>();

    public FrameFPSControl(ComponentObserver componentObserver) {
        this.componentObserver = componentObserver;
    }

    /**
     * 执行在子线程中
     *
     * @param list
     */
    public void doRecollect(List<FrameDataItem> list) {
        // 遍历合成最终数据
        for (FrameDataItem frameDataItem : list) {
            if (frameDataItem.activity == null) {
                continue;
            }
            if (!frameDataItem.isVsyncFrame) {
                continue;
            }

            // 获取对应的数据 item
            RealFrameFPSItem realFrameFPSItem = map.get(frameDataItem.activity);
            if (realFrameFPSItem == null) {
                realFrameFPSItem = new RealFrameFPSItem(frameDataItem.activity);
                map.put(frameDataItem.activity, realFrameFPSItem);
            }

            // 将多次事件的数据进行合并
            realFrameFPSItem.collect(frameDataItem.activity, frameDataItem.startNs, frameDataItem.endNs,
                    frameDataItem.dropFrame, frameDataItem.intendedFrameTimeNs);

            // 统计的总时间是否达到极限
            if (realFrameFPSItem.totalTimes > uploadDataTimeLimit) {
                map.remove(frameDataItem.activity);
                // 上报
                realFrameFPSItem.report(componentObserver);
            }
        }

    }


    private class RealFrameFPSItem {
        public long totalTimes;// 统计的总时间
        public int dropFrameCount;// 掉帧数量总和
        public int frameCount;// 帧数计算

        public int[] dropFrameLevel = new int[DropStatus.values().length];// 统计该次上报中每个掉帧等级的总和
        public int[] dropFrameSum = new int[DropStatus.values().length];// 统计该次上报中每每个掉帧等级中中掉帧数量的总和

        String visibleScene;

        public RealFrameFPSItem(String visibleScene) {
            this.visibleScene = visibleScene;
        }

        public void collect(String topActivity, long startNas, long endNas, int dropFrame, long frameIntervalNanos) {
            // 得到预期完成时间，秒！
            float frameIntervalCost = frameIntervalNanos / 1000000f;
            // 通过掉帧数计算该次时间按照预期时间上花费时间，为什么要加 1？没掉帧说明在预期时间内完成了，掉一帧说明用了两帧的时间，以此类推。
            totalTimes += (dropFrame + 1) * frameIntervalCost;

            dropFrameCount += dropFrame;
            // 一次一帧
            frameCount++;

            // 根据腾讯 Matrix 对掉帧的成都来进行分级，并记录该次掉帧数量和等级
            if (dropFrame >= frozen) {
                // 最高级冻帧
                dropFrameLevel[DropStatus.DROPPED_FROZEN.index]++;
                dropFrameSum[DropStatus.DROPPED_FROZEN.index] += dropFrame;
            } else if (dropFrame >= high) {
                // 高
                dropFrameLevel[DropStatus.DROPPED_HIGH.index]++;
                dropFrameSum[DropStatus.DROPPED_HIGH.index] += dropFrame;
            } else if (dropFrame >= middle) {
                // 中
                dropFrameLevel[DropStatus.DROPPED_MIDDLE.index]++;
                dropFrameSum[DropStatus.DROPPED_MIDDLE.index] += dropFrame;
            } else if (dropFrame >= normal) {
                // 自然
                dropFrameLevel[DropStatus.DROPPED_NORMAL.index]++;
                dropFrameSum[DropStatus.DROPPED_NORMAL.index] += dropFrame;
            } else {
                // best 最好的
                dropFrameLevel[DropStatus.DROPPED_BEST.index]++;
                dropFrameSum[DropStatus.DROPPED_BEST.index] += dropFrame;
            }

        }

        public void report(ComponentObserver componentObserver) {
            // 组织数据，马上上报

            // 计算平均 fps
            float fps = Math.min(60.f, 1000.f * frameCount / totalTimes);

            // 掉帧等级
            JSONObject dropLevelObject = new JSONObject();
            try {
                dropLevelObject.put(DropStatus.DROPPED_FROZEN.name(), dropFrameLevel[DropStatus.DROPPED_FROZEN.index]);

                dropLevelObject.put(DropStatus.DROPPED_HIGH.name(), dropFrameLevel[DropStatus.DROPPED_HIGH.index]);
                dropLevelObject.put(DropStatus.DROPPED_MIDDLE.name(), dropFrameLevel[DropStatus.DROPPED_MIDDLE.index]);
                dropLevelObject.put(DropStatus.DROPPED_NORMAL.name(), dropFrameLevel[DropStatus.DROPPED_NORMAL.index]);
                dropLevelObject.put(DropStatus.DROPPED_BEST.name(), dropFrameLevel[DropStatus.DROPPED_BEST.index]);
                // 掉帧总数统计
                JSONObject dropSumObject = new JSONObject();
                dropSumObject.put(DropStatus.DROPPED_FROZEN.name(), dropFrameSum[DropStatus.DROPPED_FROZEN.index]);
                dropSumObject.put(DropStatus.DROPPED_HIGH.name(), dropFrameSum[DropStatus.DROPPED_HIGH.index]);
                dropSumObject.put(DropStatus.DROPPED_MIDDLE.name(), dropFrameSum[DropStatus.DROPPED_MIDDLE.index]);
                dropSumObject.put(DropStatus.DROPPED_NORMAL.name(), dropFrameSum[DropStatus.DROPPED_NORMAL.index]);
                dropSumObject.put(DropStatus.DROPPED_BEST.name(), dropFrameSum[DropStatus.DROPPED_BEST.index]);
                // 最终提交的数据内容
                JSONObject resultObject = new JSONObject();
                // 当前 activity
                resultObject.put("ISSUE_SCENE", visibleScene);
                // 掉帧等级
                resultObject.put("ISSUE_DROP_LEVEL", dropLevelObject);
                // 掉帧数量
                resultObject.put("ISSUE_DROP_SUM", dropSumObject);
                // 平均 fps
                resultObject.put("FPS", fps);

                Issue issue = new Issue();
                issue.data = resultObject;

                if (componentObserver != null){
                    componentObserver.onDetectIssue(issue);
                }

                recycle();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void recycle() {

        }
    }

    public enum DropStatus {
        DROPPED_FROZEN(4), DROPPED_HIGH(3), DROPPED_MIDDLE(2), DROPPED_NORMAL(1), DROPPED_BEST(0);
        public int index;

        DropStatus(int index) {
            this.index = index;
        }

    }

}
