package com.live.simple2;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

/**
 * 单一的滚动广告
 */
public class RuningLayout extends FrameLayout implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener,Runnable {
    private static final long DELAT_TIME_FRIST = 1000;
    private static final long DELAT_TIME_USUAL = 1000;
    private ValueAnimator engine;

    private int textId = R.id.content;

    private View onlyView;

    private TextView textView;

    /**
     * 速度 dp/s
     */
    private double speed = 50;

    private boolean isRuning = false;

    private String waitContent;
    private String currentContent;


    public RuningLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RuningLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onlyView = getChildAt(0);
        textView = findViewById(textId);
    }


    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        // 子 View 想多长多长
        int newSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        super.measureChildWithMargins(child, newSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (engine == null) {
            onlyView.setVisibility(INVISIBLE);
            createEngine();
        }
        super.onLayout(changed, l, t, r, b);
    }

    private ValueAnimator createEngine() {
        if (engine == null) {
            engine = new ValueAnimator();
            engine.setInterpolator(null);

            engine.addUpdateListener(this);
            engine.addListener(this);
        }
        return engine;
    }

    private void resetEngine() {
        int selfWidth = getWidth();
        int onlyViewWidth = onlyView.getWidth();
        engine.setIntValues(selfWidth, -onlyViewWidth);
        engine.setDuration(computeSpeed(selfWidth + onlyViewWidth));
    }

    /**
     * 根据速度计算一次时间
     * @param distance
     * @return
     */
    private long computeSpeed(int distance) {
        return (long) ( distance / 3 / speed) * 1000;
    }

    @Override
    public void onAnimationStart(Animator animation) {
        isRuning = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isRuning = false;
        if (waitContent != null){
            currentContent = waitContent;
            waitContent = null;
        }
        postContentAndRun(DELAT_TIME_USUAL);
    }

    private void postContentAndRun(long delatTimeUsual) {
        textView.setText("公告："+currentContent);
        textView.postDelayed(this, delatTimeUsual);
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        scrollTo(-(int) animation.getAnimatedValue(), 0);
    }

    public void update(String content) {
        String adContent = content.replaceAll("(\n)", " ");
        if (isRuning){
            // 如果正在跑，临时保存，下一次在显示新的内容
            waitContent = adContent;
            return;
        }
        currentContent = adContent;
        postContentAndRun(DELAT_TIME_FRIST);
    }

    @Override
    public void run() {
        resetEngine();
        // 内容更新，从新获取 onlyView 宽度
        onlyView.setVisibility(VISIBLE);
        engine.start();
    }
}

