package com.live.simple2.proformance;

import android.os.Looper;

import java.util.List;

/**
 *
 */
public class SelfMonitorManager {

    private List<Component> components;

    private SelfMonitorManager(){

    }

    public static SelfMonitorManager getInstance(){
        return new SelfMonitorManager();
    }

    public void init(CompoentListener compoentListener){
        components = compoentListener.collect();

        for (int i = 0; i < components.size(); i++ ){
            components.get(i).init();
        }

        // 启动
        start();
    }

    private void start() {
        for (int i = 0; i < components.size(); i++ ){
            components.get(i).startTrace();
        }
    }
}
