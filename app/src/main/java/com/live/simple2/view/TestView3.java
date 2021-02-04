package com.live.simple2.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

public class TestView3 extends ViewGroup {
    public TestView3(Context context) {
        super(context);
    }

    public TestView3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }
}

