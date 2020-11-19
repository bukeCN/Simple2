package com.tencent.matrix.trace.tracer;

import android.os.Handler;
import android.os.SystemClock;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.report.Issue;
import com.tencent.matrix.trace.TracePlugin;
import com.tencent.matrix.trace.config.SharePluginInfo;
import com.tencent.matrix.trace.config.TraceConfig;
import com.tencent.matrix.trace.constants.Constants;
import com.tencent.matrix.trace.core.UIThreadMonitor;
import com.tencent.matrix.trace.listeners.IDoFrameListener;
import com.tencent.matrix.trace.util.Utils;
import com.tencent.matrix.util.DeviceUtil;
import com.tencent.matrix.util.MatrixHandlerThread;
import com.tencent.matrix.util.MatrixLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;

public class FrameTracer extends Tracer {

    private static final String TAG = "Matrix.FrameTracer";
    private final HashSet<IDoFrameListener> listeners = new HashSet<>();
    private final long frameIntervalNs;
    private final TraceConfig config;
    private long timeSliceMs;
    private boolean isFPSEnable;
    private long frozenThreshold;
    private long highThreshold;
    private long middleThreshold;
    private long normalThreshold;
    private int droppedSum = 0;
    private long durationSum = 0;

    public FrameTracer(TraceConfig config) {
        this.config = config;
        this.frameIntervalNs = UIThreadMonitor.getMonitor().getFrameIntervalNanos();
        this.timeSliceMs = config.getTimeSliceMs();
        this.isFPSEnable = config.isFPSEnable();
        this.frozenThreshold = config.getFrozenThreshold();
        this.highThreshold = config.getHighThreshold();
        this.normalThreshold = config.getNormalThreshold();
        this.middleThreshold = config.getMiddleThreshold();

        MatrixLog.i(TAG, "[init] frameIntervalMs:%s isFPSEnable:%s", frameIntervalNs, isFPSEnable);
        if (isFPSEnable) {
            addListener(new FPSCollector());
        }
    }

    public void addListener(IDoFrameListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(IDoFrameListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    // 父类的 onStartTrace() 方法中会调用该方法
    @Override
    public void onAlive() {
        super.onAlive();
        // 向 UIThreadMonitor 中添加 LooperObserver 观察者。
        UIThreadMonitor.getMonitor().addObserver(this);
    }

    @Override
    public void onDead() {
        super.onDead();
        UIThreadMonitor.getMonitor().removeObserver(this);
    }

    @Override
    public void doFrame(String focusedActivity, long startNs, long endNs, boolean isVsyncFrame, long intendedFrameTimeNs, long inputCostNs, long animationCostNs, long traversalCostNs) {
        // 判断 App 是否位于前台
        if (isForeground()) {
            notifyListener(focusedActivity, startNs, endNs, isVsyncFrame, intendedFrameTimeNs, inputCostNs, animationCostNs, traversalCostNs);
        }
    }

    public int getDroppedSum() {
        return droppedSum;
    }

    public long getDurationSum() {
        return durationSum;
    }

    // 当前 Activity、本次 Message 消息处理开始时间、本次 Message 消息处理结束时间、是否是由 VSYNC 信号触发的消息、vsync 信号到来时间
    // INPUT 时间耗时、ANIMATION 事件耗时、TRAVERSAL 事件耗时
    private void notifyListener(final String focusedActivity, final long startNs, final long endNs, final boolean isVsyncFrame,
                                final long intendedFrameTimeNs, final long inputCostNs, final long animationCostNs, final long traversalCostNs) {
        long traceBegin = System.currentTimeMillis();
        try {
            // 本次 vsync 信号耗时，
            final long jiter = endNs - intendedFrameTimeNs;
            // 本次 vsync信号耗时 / 预期的每帧耗时耗时 = ？？？？意义？ 15 / 16 大于 1 的说明存在掉帧
            // 这里用来 int 强转，小于 1 为 0, 大于 1 小于 2 为 1。
            // 计算掉帧数
            final int dropFrame = (int) (jiter / frameIntervalNs);
            // 计算掉帧总和，
            droppedSum += dropFrame;
            // 计算耗时时间的总和，注意：和预期相比去大的值
            durationSum += Math.max(jiter, frameIntervalNs);
            // 数据，上报模块
            synchronized (listeners) {
                for (final IDoFrameListener listener : listeners) {
                    if (config.isDevEnv()) {
                        listener.time = SystemClock.uptimeMillis();
                    }
                    // 该 Listener 为 IDpFrameListener ，FrameTracer 添加了 FPSCollerctor
                    if (null != listener.getExecutor()) {
                        if (listener.getIntervalFrameReplay() > 0) {
                            // 数据未达到上报量，先保存，暂时不上报，如：FPSCollector 设置为 200
                            listener.collect(focusedActivity, startNs, endNs, dropFrame, isVsyncFrame,
                                    intendedFrameTimeNs, inputCostNs, animationCostNs, traversalCostNs);
                        } else {
                            // 启动线程池处理数据上报，这里 FPSCollector 在子线程处理。
                            listener.getExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    listener.doFrameAsync(focusedActivity, startNs, endNs, dropFrame, isVsyncFrame,
                                            intendedFrameTimeNs, inputCostNs, animationCostNs, traversalCostNs);
                                }
                            });
                        }
                    } else {
                        // 实时处理上报
                        listener.doFrameSync(focusedActivity, startNs, endNs, dropFrame, isVsyncFrame,
                                intendedFrameTimeNs, inputCostNs, animationCostNs, traversalCostNs);
                    }

                    if (config.isDevEnv()) {
                        listener.time = SystemClock.uptimeMillis() - listener.time;
                        MatrixLog.d(TAG, "[notifyListener] cost:%sms listener:%s", listener.time, listener);
                    }
                }
            }
        } finally {
            long cost = System.currentTimeMillis() - traceBegin;
            if (config.isDebug() && cost > frameIntervalNs) {
                MatrixLog.w(TAG, "[notifyListener] warm! maybe do heavy work in doFrameSync! size:%s cost:%sms", listeners.size(), cost);
            }
        }
    }


    private class FPSCollector extends IDoFrameListener {

        private Handler frameHandler = new Handler(MatrixHandlerThread.getDefaultHandlerThread().getLooper());

        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                frameHandler.post(command);
            }
        };

        private HashMap<String, FrameCollectItem> map = new HashMap<>();

        @Override
        public Executor getExecutor() {
            return executor;
        }

        @Override
        public int getIntervalFrameReplay() {
            return 200;
        }

        // 执行数据处理，执行在子线程
        // FPSCollector 是在数量达到 200 的时候进行上报
        @Override
        public void doReplay(List<FrameReplay> list) {
            super.doReplay(list);
            // 循环调用，合并数据
            for (FrameReplay replay : list) {
                doReplayInner(replay.focusedActivity, replay.startNs, replay.endNs, replay.dropFrame, replay.isVsyncFrame,
                        replay.intendedFrameTimeNs, replay.inputCostNs, replay.animationCostNs, replay.traversalCostNs);
            }
        }

        public void doReplayInner(String visibleScene, long startNs, long endNs, int droppedFrames,
                                  boolean isVsyncFrame, long intendedFrameTimeNs, long inputCostNs,
                                  long animationCostNs, long traversalCostNs) {

            if (Utils.isEmpty(visibleScene)) return;
            // 非 vsync 垂直信号不做操作
            if (!isVsyncFrame) return;
            // 通过 activity 来获取上报数据的 item
            FrameCollectItem item = map.get(visibleScene);
            if (null == item) {
                item = new FrameCollectItem(visibleScene);
                map.put(visibleScene, item);
            }
            // 合并数据
            item.collect(droppedFrames);

            // 判断是否执行上报，当收集的数量量达到 10 秒。
            if (item.sumFrameCost >= timeSliceMs) { // report
                // 达到条件，上报服务器
                // 删除该 activity 的消息。
                map.remove(visibleScene);
                item.report();
            }
        }
    }

    private class FrameCollectItem {
        String visibleScene;
        long sumFrameCost;
        int sumFrame = 0;
        int sumDroppedFrames;
        // record the level of frames dropped each time
        int[] dropLevel = new int[DropStatus.values().length];
        int[] dropSum = new int[DropStatus.values().length];

        FrameCollectItem(String visibleScene) {
            this.visibleScene = visibleScene;
        }
        // 合并数据，并预处理数据
        void collect(int droppedFrames) {
            //  ??????? 转换成秒？后续以秒为单位
            float frameIntervalCost = 1f * UIThreadMonitor.getMonitor().getFrameIntervalNanos() / Constants.TIME_MILLIS_TO_NANO;
            // 计算区间内帧花费的时间总和，当达到预设值时才进行上报处理
            sumFrameCost += (droppedFrames + 1) * frameIntervalCost;
            // 计算掉帧的数量总和
            sumDroppedFrames += droppedFrames;
            // 计算区间内总帧数
            sumFrame++;

            // 根据掉帧数区分该次掉帧等级，具体可以参考官方文档
            // 统计收集指定区间内的掉帧情况
            if (droppedFrames >= frozenThreshold) {
                dropLevel[DropStatus.DROPPED_FROZEN.index]++;
                dropSum[DropStatus.DROPPED_FROZEN.index] += droppedFrames;
            } else if (droppedFrames >= highThreshold) {
                dropLevel[DropStatus.DROPPED_HIGH.index]++;
                dropSum[DropStatus.DROPPED_HIGH.index] += droppedFrames;
            } else if (droppedFrames >= middleThreshold) {
                dropLevel[DropStatus.DROPPED_MIDDLE.index]++;
                dropSum[DropStatus.DROPPED_MIDDLE.index] += droppedFrames;
            } else if (droppedFrames >= normalThreshold) {
                dropLevel[DropStatus.DROPPED_NORMAL.index]++;
                dropSum[DropStatus.DROPPED_NORMAL.index] += droppedFrames;
            } else {
                dropLevel[DropStatus.DROPPED_BEST.index]++;
                dropSum[DropStatus.DROPPED_BEST.index] += Math.max(droppedFrames, 0);
            }
        }

        // 回调上报数据
        // 这里统计的是一段时间区间内的数据！！！
        void report() {
            // 计算平均 fps
            float fps = Math.min(60.f, 1000.f * sumFrame / sumFrameCost);
            MatrixLog.i(TAG, "[report] FPS:%s %s", fps, toString());

            try {
                TracePlugin plugin = Matrix.with().getPluginByClass(TracePlugin.class);
                if (null == plugin) {
                    return;
                }
                // 掉帧等级
                JSONObject dropLevelObject = new JSONObject();
                dropLevelObject.put(DropStatus.DROPPED_FROZEN.name(), dropLevel[DropStatus.DROPPED_FROZEN.index]);
                dropLevelObject.put(DropStatus.DROPPED_HIGH.name(), dropLevel[DropStatus.DROPPED_HIGH.index]);
                dropLevelObject.put(DropStatus.DROPPED_MIDDLE.name(), dropLevel[DropStatus.DROPPED_MIDDLE.index]);
                dropLevelObject.put(DropStatus.DROPPED_NORMAL.name(), dropLevel[DropStatus.DROPPED_NORMAL.index]);
                dropLevelObject.put(DropStatus.DROPPED_BEST.name(), dropLevel[DropStatus.DROPPED_BEST.index]);
                // 掉帧总数统计
                JSONObject dropSumObject = new JSONObject();
                dropSumObject.put(DropStatus.DROPPED_FROZEN.name(), dropSum[DropStatus.DROPPED_FROZEN.index]);
                dropSumObject.put(DropStatus.DROPPED_HIGH.name(), dropSum[DropStatus.DROPPED_HIGH.index]);
                dropSumObject.put(DropStatus.DROPPED_MIDDLE.name(), dropSum[DropStatus.DROPPED_MIDDLE.index]);
                dropSumObject.put(DropStatus.DROPPED_NORMAL.name(), dropSum[DropStatus.DROPPED_NORMAL.index]);
                dropSumObject.put(DropStatus.DROPPED_BEST.name(), dropSum[DropStatus.DROPPED_BEST.index]);
                // 最终提交的数据内容
                JSONObject resultObject = new JSONObject();
                // 设备信息
                resultObject = DeviceUtil.getDeviceInfo(resultObject, plugin.getApplication());
                // 当前 activity
                resultObject.put(SharePluginInfo.ISSUE_SCENE, visibleScene);
                // 掉帧等级
                resultObject.put(SharePluginInfo.ISSUE_DROP_LEVEL, dropLevelObject);
                // 掉帧数量
                resultObject.put(SharePluginInfo.ISSUE_DROP_SUM, dropSumObject);
                // 平均 fps
                resultObject.put(SharePluginInfo.ISSUE_FPS, fps);

                Issue issue = new Issue();
                issue.setTag(SharePluginInfo.TAG_PLUGIN_FPS);
                issue.setContent(resultObject);
                plugin.onDetectIssue(issue);

            } catch (JSONException e) {
                MatrixLog.e(TAG, "json error", e);
            } finally {
                // 一次数据上报完毕，充值
                sumFrame = 0;
                sumDroppedFrames = 0;
                sumFrameCost = 0;
            }
        }


        @Override
        public String toString() {
            return "visibleScene=" + visibleScene
                    + ", sumFrame=" + sumFrame
                    + ", sumDroppedFrames=" + sumDroppedFrames
                    + ", sumFrameCost=" + sumFrameCost
                    + ", dropLevel=" + Arrays.toString(dropLevel);
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
