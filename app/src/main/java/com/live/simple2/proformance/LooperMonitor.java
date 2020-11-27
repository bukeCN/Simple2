package com.live.simple2.proformance;

import android.os.Looper;
import android.util.Printer;

import java.util.ArrayList;
import java.util.List;


/**
 * 监控主线程 Message 消息执行
 * 因为在这个组件可以内部运行不用依赖外界，因此全都自己处理了，只暴露添加监听的函数。
 */
public class LooperMonitor  implements Printer {

    private static final LooperMonitor mMonitor = new LooperMonitor();

    private List<LooperObserver> looperObserverList = new ArrayList<>();

    public LooperMonitor(){
        this(Looper.getMainLooper());
    }

    public LooperMonitor(Looper looper){
        looper.setMessageLogging(this);
    }

    @Override
    public void println(String x) {
        char frist = x.charAt(0);
        if (frist == '>'){
            // 消息开始执行
            dispatchStart();
        } else {
            // 消息执行结束
            dispatchEnd();
        }
    }

    private void dispatchEnd() {
        for (LooperObserver looperObserver : looperObserverList ){
            looperObserver.dispatchEnd();
        }
    }

    private void dispatchStart() {
        for (LooperObserver looperObserver : looperObserverList ){
            looperObserver.dispatchStart();
        }
    }

    public static void registObserver(LooperObserver looperObserver){
        mMonitor.looperObserverList.add(looperObserver);
    }
    public static void unRegistObserver(LooperObserver looperObserver){
        mMonitor.looperObserverList.add(looperObserver);
    }


}
