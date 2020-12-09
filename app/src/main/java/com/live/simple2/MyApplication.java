package com.live.simple2;

import android.app.Application;

import com.live.simple2.proformance.CompoentListener;
import com.live.simple2.proformance.Component;
import com.live.simple2.proformance.SelfMonitorManager;
import com.live.simple2.proformance.compoent.UIThreadComponent;
import com.live.simple2.proformance.trace.UITraceControl;

import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application {



    @Override
    public void onCreate() {
        super.onCreate();

        SelfMonitorManager.getInstance().init(new CompoentListener() {
            @Override
            public List<Component> collect() {
                List<Component> traceComponents = new LinkedList<>();
                traceComponents.add(new UIThreadComponent());
                return traceComponents;
            }
        });
    }
}
