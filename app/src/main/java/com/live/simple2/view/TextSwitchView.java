package com.live.simple2.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.ViewCompat;

/**
 * 开关指示器 view
 * 不支持 Padding
 */
public class TextSwitchView extends View implements ValueAnimator.AnimatorUpdateListener {
    private String offText = "不需要";// 开文字
    private String onText = "需要";// 关文字

    private int textSize = 10;// 文字大小

    private int textBetweenDis = 10;// 文字之间的距离

    private int bgRadius = 0;// 背景圆角大小

    private float inSidePadding = dp2px(2);// 指示器和背景圆角间隙

    private float flagRadius = 1;// 指示器圆角大小，默认为背景高度的一般减去间隙得值

    private int bgColor = Color.parseColor("#db9d45");// 背景颜色

    private int flagBgColor = Color.WHITE;// 指示器背景颜色

    private int flagTextColor = Color.parseColor("#db9d45");// 指示器文字颜色

    private int bgTextColor = Color.parseColor("#A8ffffff");// 背景文字颜色

    private int flagBgShadowColor = Color.parseColor("#3b000000");// 指示器阴影颜色

    private Path bgShadowPath;// 用于绘制背景内阴影的 path, 待优化

    private int viewMeasureWidth, viewMeasureHeight;

    private Paint textPaint, bgPaint, flagPaint, bgShadowPaint;// 文字、背景、指示器画笔、背景内阴影画笔

    private Paint.FontMetrics fontMetrics;// 文字位置辅助类
    private Rect offTextBounds, onTextBounds;// 文字位置辅助

    private RectF bgRect, flagRect;

    private Point offTextStartPoint, onTextStartPoint;// 文字绘制起始点

    private boolean isChecked = false;// 是否选中

    private ValueAnimator moveAnimator;

    private float flagOffsetX = 0;// 滑动偏移量，根据动画实时变动

    private OnCheckedListener onCheckedListener;

    public TextSwitchView(Context context) {
        super(context);
        init(context);
    }

    public TextSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewMeasureWidth = getMeasuredWidth();
        viewMeasureHeight = getMeasuredHeight();

        bgRadius = viewMeasureHeight / 2;
        flagRadius = (viewMeasureHeight - inSidePadding * 2) / 2;

        bgRect = new RectF();
        bgRect.left = 0;
        bgRect.top = 0;
        bgRect.right = bgRect.left + viewMeasureWidth;
        bgRect.bottom = bgRect.top + viewMeasureHeight;

        if (isChecked){
            flagOffsetX = viewMeasureWidth / 2f;
        } else {
            flagOffsetX = inSidePadding;
        }

        bgShadowPath.addRect(bgRect, Path.Direction.CCW);

        // 指示器矩形计算
        flagRect = new RectF();
        flagRect.left = bgRect.left + flagOffsetX;
        flagRect.top = bgRect.top + inSidePadding;
        flagRect.right = viewMeasureWidth / 2f + flagRect.left;
        flagRect.bottom = viewMeasureHeight - inSidePadding;

        // 确定文字绘制 x / y  轴起始点
        offTextStartPoint.x = viewMeasureWidth / 4 - (offTextBounds.right - offTextBounds.left) / 2;
        // 计算 y 轴位置，https://blog.csdn.net/liangfeng093/article/details/84847304
        offTextStartPoint.y = (int) (viewMeasureHeight / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
        onTextStartPoint.x = (int) (viewMeasureWidth * 0.75f - (onTextBounds.right - onTextBounds.left) / 2);
        onTextStartPoint.y = (int) (viewMeasureHeight / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
    }

    public void init(Context context) {
        // 初始化画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(sp2px(textSize));
        textPaint.setColor(bgTextColor);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);

        bgShadowPaint = new Paint(bgPaint);
        bgShadowPaint.setStyle(Paint.Style.STROKE);
        bgShadowPaint.setShadowLayer(dp2px(3), 0, 1, flagBgShadowColor);

        flagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagPaint.setColor(flagBgColor);
        flagPaint.setShadowLayer(dp2px(2), 0, 1, flagBgShadowColor);// 指示器外部阴影

        // 测量文字，得出绘制信息
        offTextBounds = new Rect();
        onTextBounds = new Rect();
        fontMetrics = textPaint.getFontMetrics();
        textPaint.getTextBounds(offText, 0, offText.length(), offTextBounds);
        textPaint.getTextBounds(onText, 0, onText.length(), onTextBounds);

        offTextStartPoint = new Point();
        onTextStartPoint = new Point();

        bgShadowPath = new Path();


        setOnClickListener(view -> {
            isChecked = !isChecked;
            updataStatus(isChecked);
            if (onCheckedListener != null) {
                onCheckedListener.onChecked(isChecked);
            }
        });
    }

    private void updataStatus(boolean checked) {
        isChecked = checked;
        if (isChecked) {
            getMoveAnimator().start();
        } else {
            getMoveAnimator().reverse();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制背景
        canvas.drawRoundRect(bgRect, bgRadius, bgRadius, bgPaint);

        // 绘制两端文字
        textPaint.setColor(bgTextColor);
        canvas.drawText(offText, offTextStartPoint.x, offTextStartPoint.y, textPaint);
        canvas.drawText(onText, onTextStartPoint.x, onTextStartPoint.y, textPaint);

        // 设置偏移
        flagRect.left = flagOffsetX;
        flagRect.right = flagRect.left + viewMeasureWidth / 2f;
        // 绘制指示器
        canvas.drawRoundRect(flagRect, flagRadius, flagRadius, flagPaint);

        // 在绘制一次文字，跟着指示器走
        if (!getMoveAnimator().isRunning()) {
            textPaint.setColor(flagTextColor);
            if (isChecked) {
                canvas.drawText(onText, onTextStartPoint.x, onTextStartPoint.y, textPaint);
            } else {
                canvas.drawText(offText, offTextStartPoint.x, offTextStartPoint.y, textPaint);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (moveAnimator != null) {
            moveAnimator.cancel();
        }
    }

    public ValueAnimator getMoveAnimator() {
        if (moveAnimator == null) {
            float value = getAnimatorLimitValue();
            moveAnimator = ValueAnimator.ofFloat(inSidePadding, value);
            moveAnimator.setDuration(200);
            moveAnimator.addUpdateListener(this);
        }
        return moveAnimator;
    }

    private float getAnimatorLimitValue() {
        return viewMeasureWidth / 2f - inSidePadding;
    }

    public static float dp2px(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static float sp2px(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, Resources.getSystem().getDisplayMetrics());
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        flagOffsetX = (float) animation.getAnimatedValue();
        invalidate();
    }

    public OnCheckedListener getOnCheckedListener() {
        return onCheckedListener;
    }

    public void setOnCheckedListener(OnCheckedListener onCheckedListener) {
        this.onCheckedListener = onCheckedListener;
    }

    public void setChecked(boolean checked) {
        if (isChecked == checked) {
            return;
        }
        isChecked = checked;
        // 解决在 onCreate 中调用问题
        if (isAttachedToWindow() && isLaidOut()) {
            updataStatus(isChecked);
        } else {
            cancelPositionAnimator();
            invalidate();
        }
    }

    private void cancelPositionAnimator() {
        if (moveAnimator != null) {
            moveAnimator.cancel();
        }
    }


    public interface OnCheckedListener {
        void onChecked(boolean isChecked);
    }
}
