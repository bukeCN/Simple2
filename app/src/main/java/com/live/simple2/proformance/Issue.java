package com.live.simple2.proformance;

import org.json.JSONObject;

/**
 * 上传数据基类
 */
public class Issue {
    public JSONObject data;

    @Override
    public String toString() {
        return "Issue{" +
                "data=" + data.toString() +
                '}';
    }
}
