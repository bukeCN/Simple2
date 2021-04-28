package com.live.simple2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class TestViewGroup extends FrameLayout {
    int i = 0;

    public TestViewGroup(Context context) {
        super(context);
    }

    public TestViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        Log.e("sun", "requestLayout");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e("sun", "onLayout");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("sun", "onMeasure");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        throw new NullPointerException("");
    }

    private float x;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            x = ev.getX();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float moveX = ev.getX();
            float absMove = Math.abs(moveX - x);
            if (absMove > 200) {
                Log.e("sun", "拦截" + moveX);
                return true;
            } else {
                Log.e("sun", "不拦截" + moveX);
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("sun", "处理了" + event.getX());
        return true;
    }
}
