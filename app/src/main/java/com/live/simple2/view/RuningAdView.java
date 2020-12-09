package com.live.simple2.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.live.simple2.R;
import com.live.simple2.utils.DeviceUtil;
import com.live.simple2.utils.StringUtil;

/**
 * 滚动广告 View
 */
public class RuningAdView extends FrameLayout implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener, Runnable {
    private static final long DELAT_TIME_FRIST = 100;
    private static final long DELAT_TIME_USUAL = 1000 * 2;
    private ValueAnimator engine;

    private int textId = 0;

    private View onlyView;

    private TextView textView;

    /**
     * 速度 dp/s
     */
    private double speed = 500;

    private boolean isRuning = false;
    // 是否中断
    private boolean isBreak = false;

    private String waitContent;
    private String currentContent;


    public RuningAdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public RuningAdView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        createEngine();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.e("sun","onGlobalLayout");
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.e("sun","onAttachedToWindow");
        onlyView = getChildAt(0);
        textView = findViewById(textId);

        if (onlyView != null) {
            onlyView.setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (engine != null) {
            engine.cancel();
        }
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        // 子 View 想多长多长
        int newSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        super.measureChildWithMargins(child, newSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
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
        int onlyViewWidth = 0;
        if (onlyView != null) {
            onlyViewWidth = onlyView.getWidth();
        }
        if (engine == null) {
            createEngine();
        }
        engine.setIntValues(selfWidth, -onlyViewWidth);
        engine.setDuration(computeSpeed(selfWidth + onlyViewWidth));
    }

    /**
     * 根据速度计算一次时间
     *
     * @param distance
     * @return
     */
    private long computeSpeed(int distance) {
        return (long) (DeviceUtil.px2dp(distance) / speed) * 1000;
    }

    @Override
    public void onAnimationStart(Animator animation) {
        isRuning = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isRuning = false;
        if (!StringUtil.isEmpty(waitContent)) {
            currentContent = waitContent;
            waitContent = null;
        }
        // 判断是否终止
        if (!isBreak) {
            postContentAndRun(DELAT_TIME_USUAL);
        }
    }

    private void postContentAndRun(long delatTimeUsual) {
        if(textView != null){
            textView.setText("公告：" + currentContent);
            textView.postDelayed(this, delatTimeUsual);
        }
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

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Log.e("sun","onSaveInstanceState");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.e("sun","onRestoreInstanceState");
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        Log.e("sun","onWindowVisibilityChanged: "+ visibility);
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("sun","onSizeChanged: "+ w + "-" + h );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("sun","onMeasure");

        getWidth();
        getMeasuredWidth();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        Log.e("sun","onWindowFocusChanged: "+ hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.e("sun","onFinishInflate"+getChildCount());
    }

    @Override
    public void draw(Canvas canvas) {
        Log.e("sun","draw");
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("sun","onDraw");
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e("sun","onLayout："+getChildCount()+"**"+getWidth());
    }



    public void update(String content) {
        if (StringUtil.isEmpty(content)) {
            // 不显示内容, 如果当前正在播放中，那么下一次停止播放
            isBreak = true;
            return;
        }
        // 重置中断
        isBreak = false;
        // 去空格
        String adContent = content.replaceAll("(\n)", " ");
        if (isRuning) {
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
        if (onlyView != null){
            onlyView.setVisibility(VISIBLE);
            engine.start();
        }
    }
}
