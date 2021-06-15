package com.live.simple2;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Trace;

import com.live.simple2.proformance.CompoentListener;
import com.live.simple2.proformance.Component;
import com.live.simple2.proformance.SelfMonitorManager;
import com.live.simple2.proformance.compoent.UIThreadComponent;
import com.live.simple2.proformance.trace.UITraceControl;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application {

    Handler handler;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();


//        SelfMonitorManager.getInstance().init(new CompoentListener() {
//            @Override
//            public List<Component> collect() {
//                List<Component> traceComponents = new LinkedList<>();
//                traceComponents.add(new UIThreadComponent());
//                return traceComponents;
//            }
//        });

    }
}
