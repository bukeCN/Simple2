package com.live.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class TestViewGroup extends FrameLayout {

    public TestViewGroup(Context context) {
        super(context);
    }

    public TestViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float mDownX;
    private float mDownY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        boolean interceptd = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                interceptd = false;
                //测量按下位置
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //计算移动距离 判定是否滑动
                float dx = event.getX() - mDownX;
                float dy = event.getY() - mDownY;

                if (Math.abs(dx) > ViewConfiguration.get(getContext()).getScaledTouchSlop() || Math.abs(dy) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    System.out.println("拦截滑动事件");
                    interceptd = true;
                } else {
                    interceptd = false;
                }

                break;

            case MotionEvent.ACTION_UP:
                interceptd = false;
                break;
        }
        return interceptd;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - mDownX;
                float dy = event.getY() - mDownY;

                float ownX = getX();
                //获取手指按下的距离与控件本身Y轴的距离
                float ownY = getY();
                //理论中X轴拖动的距离
                float endX = ownX + dx;
                //理论中Y轴拖动的距离
                float endY = ownY + dy;
//
//                //X轴边界限制
//                endX = endX < 0 ? 0 : endX > maxX ? maxX : endX;
//                //Y轴边界限制
//                endY = endY < 0 ? 0 : endY > maxY ? maxY : endY;
                //开始移动
                setX(endX);
                setY(endY);

                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        super.onTouchEvent(event);

        return true;
    }
}
