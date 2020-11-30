package com.live.simple2.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public class DeviceUtil {
    public static float dp2px(int dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp, Resources.getSystem().getDisplayMetrics());
    }

    public static float px2dp(int px){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,px, Resources.getSystem().getDisplayMetrics());
    }
}
