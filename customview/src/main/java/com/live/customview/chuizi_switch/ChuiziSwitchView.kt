package com.live.customview.chuizi_switch

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.live.customview.Utils
import kotlin.math.roundToInt

class ChuiziSwitchView : View {
    private val dotFlagPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private val commnPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var viewConfiguration: ViewConfiguration

    private val dotFlagColors = intArrayOf(
        Color.parseColor("#9db8f9"),
        Color.parseColor("#e1e1e1")
    )
    private var dotFlagDistance = 0f
    private val dotFlagRadius = Utils.dp2px(6)
    private var touchButtonXinit = 0f

    /**
     * 触摸按钮阴影需要预留出来的空间，也是阴影扩散的大小
     */
    private var touchButtonShadowSpaceMax = Utils.dp2px(4)
    private var touchButtonShadowSpaceMin = Utils.dp2px(1)

    /**
     * 触摸按钮阴影下移位置量
     */
    private var touchButtonShadowSpaceYOffse = Utils.dp2px(2)

    private var reallyUseHeight = 0
    private var reallyUseWidth = 0

    /**
     * 开关指示器两个圆点之间的距离
     */
    private var switchFlagBetweenDistance = 0

    /**
     * 开关状态
     */
    private var switchState = false
    /**
     * 开关状态转换进度，和动画挂钩
     */
    private var switchRate = 0f

    /**
     * 触摸按钮按下状态
     */
    private var touchPushRate = 0f;

    private var touchButtonAnnotation: ValueAnimator? = null

    private var touchButtonMoveAnnotation: ValueAnimator? = null

    private fun touchButtonMoveAnnotation(value: Float): ValueAnimator?{
        if (touchButtonMoveAnnotation == null){
            touchButtonMoveAnnotation = ValueAnimator.ofFloat()
        }
        touchButtonMoveAnnotation?.setFloatValues(value)
        touchButtonMoveAnnotation?.addUpdateListener {
            needOffsetXofTouchButton = -(it.animatedValue as Float)
            invalidate()
        }
        return touchButtonMoveAnnotation
    }

    private fun touchButtonAnnotation(value: Float): ValueAnimator? {
        if (touchButtonAnnotation == null){
            touchButtonAnnotation = ValueAnimator.ofFloat()
        }
        touchButtonAnnotation?.duration = 200
        touchButtonAnnotation?.setFloatValues(value)
        touchButtonAnnotation?.addUpdateListener {
            touchPushRate = it.animatedValue as Float
            invalidate()
        }
        return touchButtonAnnotation
    }

    constructor(context: Context) : super(context){
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init(context)
    }

    private fun init(context: Context){
        viewConfiguration = ViewConfiguration.get(context)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        reallyUseHeight = (height - paddingTop - paddingBottom - 2*touchButtonShadowSpaceMax).toInt()
        reallyUseWidth = (width - paddingLeft - paddingRight - 2*touchButtonShadowSpaceMax).toInt()
        dotFlagDistance = reallyUseHeight / 2f + Utils.dp2px(4)
        touchButtonXinit = reallyUseHeight / 2f
        // 计算公式，2 * 触摸按钮圆点到初始状态flag圆点的位置。
        switchFlagBetweenDistance = (reallyUseWidth - 2f*dotFlagDistance).roundToInt()
    }

    override fun onDraw(canvas: Canvas) {
        // 这个是计算的关键，坐标移动了！！！
        canvas.translate(touchButtonShadowSpaceMax,touchButtonShadowSpaceMax - touchButtonShadowSpaceYOffse)
        drawFlag(canvas)

        drawSwitchBackground(canvas)

        drawTouchButton(canvas)
    }

    private var downX = 0f
    private var downy = 0f
    private var needOffsetXofTouchButton = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var action = event.action
        when(action){
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downy = event.y
                // 实现按下状态动画
                touchButtonAnnotation(1f)?.start()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.x
                needOffsetXofTouchButton = moveX - downX
                val willMoveDiatance = touchButtonXinit + needOffsetXofTouchButton

                if (willMoveDiatance < touchButtonXinit){
                    needOffsetXofTouchButton = touchButtonXinit
                } else {
                    if (willMoveDiatance > reallyUseWidth-touchButtonXinit){
                        needOffsetXofTouchButton = reallyUseWidth-touchButtonXinit
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchButtonAnnotation(1f)?.reverse()
                val absNeedOffsetXofTouchButton = Math.abs(needOffsetXofTouchButton)
                val canMoveDistance = (reallyUseWidth - 2f*dotFlagDistance).roundToInt()
                if (absNeedOffsetXofTouchButton > canMoveDistance/2){
                    // 切换状态

                } else {
                    // 归零
                    touchButtonMoveAnnotation(needOffsetXofTouchButton)?.start()
                }

            }
        }
        return super.onTouchEvent(event)
    }


    /**
     * 绘制触摸按钮
     * @param canvas
     */
    private fun drawTouchButton(canvas: Canvas) {
        canvas.save()

        dotFlagPaint.style = Paint.Style.FILL_AND_STROKE
        dotFlagPaint.color = Color.WHITE

        val finalShadowSpace = touchButtonShadowSpaceMin + (1 - touchPushRate) * (touchButtonShadowSpaceMax - touchButtonShadowSpaceMin)
        dotFlagPaint.setShadowLayer(finalShadowSpace,0f,touchButtonShadowSpaceYOffse,Color.parseColor("#d1d1d1"))

        val touchButtonX = touchButtonXinit + needOffsetXofTouchButton
        canvas.drawCircle(
            touchButtonX, reallyUseHeight / 2f,
            reallyUseHeight / 2f ,
            dotFlagPaint
        )
        canvas.restore()
        dotFlagPaint.clearShadowLayer()
    }


    /**
     * 绘制开关背景
     * @param canvas
     */
    private fun drawSwitchBackground(canvas: Canvas) {
        canvas.save()
        // 线上记得挪出去
        val shadowPath = Path()
        val cornerRadius = reallyUseHeight / 2f
        val shadowRectF = RectF(0f, 0f, reallyUseWidth.toFloat(), reallyUseHeight.toFloat())
        shadowPath.addRoundRect(
            shadowRectF,
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )
        commnPaint.color = Color.parseColor("#e1e1e1")
        commnPaint.style = Paint.Style.STROKE
        commnPaint.strokeWidth = Utils.dp2px(1)
        // 绘制内阴影？先给外框绘制阴影，然后再裁切掉外部阴影即可。
        commnPaint.setShadowLayer(cornerRadius / 3, 0f, 0f, Color.parseColor("#ffc1c1c1"))
        // 裁切
        canvas.clipPath(shadowPath)
        // 然后再进行绘制
        canvas.drawPath(shadowPath, commnPaint)
        commnPaint.clearShadowLayer()
        canvas.restore()
    }

    /**
     * 绘制两个圆点指示器
     * @param canvas
     */
    private fun drawFlag(canvas: Canvas) {
        dotFlagPaint.color = dotFlagColors[0]
        canvas.drawCircle(dotFlagDistance - (1 - switchRate)*switchFlagBetweenDistance, reallyUseHeight / 2f, dotFlagRadius, dotFlagPaint)
        dotFlagPaint.color = dotFlagColors[1]
        canvas.drawCircle(
            reallyUseWidth - dotFlagDistance + switchRate*switchFlagBetweenDistance,
            reallyUseHeight / 2f,
            dotFlagRadius,
            dotFlagPaint
        )
    }


}