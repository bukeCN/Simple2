package com.live.simple2.proformance;

/**
 * 卡顿监控组件
 */
public interface Component {
    void init();

    void startTrace();

    void stopTrace();
}
