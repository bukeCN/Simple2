/*
 * Tencent is pleased to support the open source community by making wechat-matrix available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.matrix.trace;

import android.app.Application;
import android.os.Build;
import android.os.Looper;

import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.plugin.PluginListener;
import com.tencent.matrix.trace.config.SharePluginInfo;
import com.tencent.matrix.trace.config.TraceConfig;
import com.tencent.matrix.trace.core.AppMethodBeat;
import com.tencent.matrix.trace.core.UIThreadMonitor;
import com.tencent.matrix.trace.tracer.AnrTracer;
import com.tencent.matrix.trace.tracer.EvilMethodTracer;
import com.tencent.matrix.trace.tracer.FrameTracer;
import com.tencent.matrix.trace.tracer.StartupTracer;
import com.tencent.matrix.util.MatrixHandlerThread;
import com.tencent.matrix.util.MatrixLog;

/**
 * Created by caichongyang on 2017/5/20.
 */
public class TracePlugin extends Plugin {
    private static final String TAG = "Matrix.TracePlugin";

    private final TraceConfig traceConfig;
    private EvilMethodTracer evilMethodTracer;
    private StartupTracer startupTracer;
    private FrameTracer frameTracer;
    private AnrTracer anrTracer;

    public TracePlugin(TraceConfig config) {
        this.traceConfig = config;
    }

    // 性能追踪模块初始化方法
    @Override
    public void init(Application app, PluginListener listener) {
        super.init(app, listener);
        MatrixLog.i(TAG, "trace plugin init, trace config: %s", traceConfig.toString());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            MatrixLog.e(TAG, "[FrameBeat] API is low Build.VERSION_CODES.JELLY_BEAN(16), TracePlugin is not supported");
            unSupportPlugin();
            return;
        }
        // ANR 模块
        anrTracer = new AnrTracer(traceConfig);
        // UI 界面相关，是否掉帧、卡顿、FPS
        frameTracer = new FrameTracer(traceConfig);
        // 耗时方法追踪，主要是检测 app 运行过程中耗时较长的方法
        evilMethodTracer = new EvilMethodTracer(traceConfig);
        // 启动耗时追踪
        startupTracer = new StartupTracer(traceConfig);
    }

    // 性能监控插件启动
    @Override
    public void start() {
        super.start();
        if (!isSupported()) {
            MatrixLog.w(TAG, "[start] Plugin is unSupported!");
            return;
        }
        MatrixLog.w(TAG, "start!");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 判断主线程监控是否初始化，如果没有初始化就调用初始化方法，这里的 UIthreadMonitor 负责监控
                // 主线的执行 message 的耗时时长和利用 IdeHandle
                if (!UIThreadMonitor.getMonitor().isInit()) {
                    try {
                        UIThreadMonitor.getMonitor().init(traceConfig);
                    } catch (java.lang.RuntimeException e) {
                        MatrixLog.e(TAG, "[start] RuntimeException:%s", e);
                        return;
                    }
                }
                // 以下依次启动各个监控模块
                AppMethodBeat.getInstance().onStart();
                // 启动UIThreaMonitor 监控
                UIThreadMonitor.getMonitor().onStart();

                anrTracer.onStartTrace();
                // 这里是重点，前面的 UIThreadMonitor 是收集数据，这里含有数据的上报。
                frameTracer.onStartTrace();

                evilMethodTracer.onStartTrace();

                startupTracer.onStartTrace();
            }
        };
        // 判断当前是否是主线程，如果是就直接运行 runnable，否则获取主线程 Handler post 任务。
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            MatrixLog.w(TAG, "start TracePlugin in Thread[%s] but not in mainThread!", Thread.currentThread().getId());
            MatrixHandlerThread.getDefaultMainHandler().post(runnable);
        }

    }

    @Override
    public void stop() {
        super.stop();
        if (!isSupported()) {
            MatrixLog.w(TAG, "[stop] Plugin is unSupported!");
            return;
        }
        MatrixLog.w(TAG, "stop!");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                AppMethodBeat.getInstance().onStop();

                UIThreadMonitor.getMonitor().onStop();

                anrTracer.onCloseTrace();

                frameTracer.onCloseTrace();

                evilMethodTracer.onCloseTrace();

                startupTracer.onCloseTrace();

            }
        };

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            MatrixLog.w(TAG, "stop TracePlugin in Thread[%s] but not in mainThread!", Thread.currentThread().getId());
            MatrixHandlerThread.getDefaultMainHandler().post(runnable);
        }

    }

    @Override
    public void onForeground(boolean isForeground) {
        super.onForeground(isForeground);
        if (!isSupported()) {
            return;
        }

        if (frameTracer != null) {
            frameTracer.onForeground(isForeground);
        }

        if (anrTracer != null) {
            anrTracer.onForeground(isForeground);
        }

        if (evilMethodTracer != null) {
            evilMethodTracer.onForeground(isForeground);
        }

        if (startupTracer != null) {
            startupTracer.onForeground(isForeground);
        }

    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getTag() {
        return SharePluginInfo.TAG_PLUGIN;
    }

    public FrameTracer getFrameTracer() {
        return frameTracer;
    }

    public AppMethodBeat getAppMethodBeat() {
        return AppMethodBeat.getInstance();
    }

    public AnrTracer getAnrTracer() {
        return anrTracer;
    }

    public EvilMethodTracer getEvilMethodTracer() {
        return evilMethodTracer;
    }

    public StartupTracer getStartupTracer() {
        return startupTracer;
    }

    public UIThreadMonitor getUIThreadMonitor() {
        if (UIThreadMonitor.getMonitor().isInit()) {
            return UIThreadMonitor.getMonitor();
        } else {
            return null;
        }
    }

    public TraceConfig getTraceConfig() {
        return traceConfig;
    }
}
