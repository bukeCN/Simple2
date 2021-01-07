package com.live.simple2.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.renderscript.Sampler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

/**
 * 支持左右两个 View ，可滑动拉出右边的 View。
 */
public class RightEdgeMoveLayout extends FrameLayout implements GestureDetector.OnGestureListener, ViewTreeObserver.OnGlobalLayoutListener, ValueAnimator.AnimatorUpdateListener {
    private static final String TAG = "RightEdgeMoveLayout";

    private View contentView;
    private View rightView;

    private float contentTranslationX;

    private int rightViewWidth;

    private GestureDetectorCompat gestureDetector;

    private ValueAnimator backToAnimation;

    private OnOpenListener onOpenListener;

    public RightEdgeMoveLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RightEdgeMoveLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetectorCompat(context,this);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2){
            Log.w(TAG, "View 的个数不符合要求！");
        }
        rightView = getChildAt(0);
        contentView = getChildAt(1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewCount = getChildCount();
        for (int i = 0; i < viewCount; i++ ){
            View child = getChildAt(i);
            if (i == 0){
                int childRight = right + child.getMeasuredWidth();
                child.layout(right,top,childRight,bottom);
            } else {
                child.layout(left,top,right,bottom);
            }
        }
    }


    boolean isHandlerUp = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = gestureDetector.onTouchEvent(event);
        if (result){
            isHandlerUp = true;
            requestDisallowInterceptTouchEvent(true);
        }
        if (isHandlerUp && event.getAction() == MotionEvent.ACTION_UP){
            if (contentTranslationX == rightViewWidth){
                // 触发监听
                if (onOpenListener != null){
                    onOpenListener.onOpen();
                }
                resetContentView();
            } else {
                // 回弹
                getBackToAnimation().start();
            }
        }
        return result;
    }

    private void resetContentView() {
        contentTranslationX = 0;
        scrollTo((int) contentTranslationX,0);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (contentView == null){
            return false;
        }
        contentTranslationX += distanceX;

        // 边界限制
        if (contentTranslationX > rightViewWidth){
            contentTranslationX = rightViewWidth;
        }
        if (contentTranslationX < 0){
            contentTranslationX = 0;
        }
        scrollTo((int) contentTranslationX,0);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onGlobalLayout() {
        rightViewWidth = rightView.getWidth();
    }

    public ValueAnimator getBackToAnimation() {
        if (backToAnimation == null){
            backToAnimation = new ValueAnimator();
        }
        backToAnimation.setFloatValues(contentTranslationX, 0);
        backToAnimation.setDuration(300);
        backToAnimation.addUpdateListener(this);
        return backToAnimation;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        contentTranslationX = (float) animation.getAnimatedValue();
        scrollTo((int) contentTranslationX,0);
    }

    public OnOpenListener getOnOpenListener() {
        return onOpenListener;
    }

    public void setOnOpenListener(OnOpenListener onOpenListener) {
        this.onOpenListener = onOpenListener;
    }

    public static interface OnOpenListener {
        void onOpen();
    }
}
