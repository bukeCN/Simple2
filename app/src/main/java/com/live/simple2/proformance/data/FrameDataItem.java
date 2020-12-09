package com.live.simple2.proformance.data;

/**
 * 一次 VSYNC 信号数据记录 item
 */
public class FrameDataItem {

    public String activity;
    public long startNs;
    public long endNs;
    public int dropFrame;
    public boolean isVsyncFrame;
    public long intendedFrameTimeNs;

}
