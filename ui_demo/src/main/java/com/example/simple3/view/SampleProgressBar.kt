package com.example.simple3.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatTextView

/**
 * 简单的，圆角的，进度条
 * @property backGroundPaint Paint
 * @property progressBarPaint Paint
 * @property backGroundRect RectF
 * @property progressBarRect RectF
 * @property roundRadius Float
 * @property currentProgress Float
 * @constructor
 */
class SampleProgressBar (context: Context, attrs: AttributeSet?): AppCompatTextView(context, attrs),
    ViewTreeObserver.OnGlobalLayoutListener {

    private var backGroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progressBarPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val backGroundRect = RectF()
    private val progressBarRect = RectF()

    private var roundRadius = 0f

    /**
     * 当前进度
     */
    var currentProgress = 0.5f
        set(value) {
            field = value
            postInvalidate()
        }

    init {
        backGroundPaint.setColor(Color.BLACK)

        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        viewTreeObserver.removeOnGlobalLayoutListener(this)
        backGroundRect.left = 0F
        backGroundRect.top = 0F
        backGroundRect.right = width.toFloat()
        backGroundRect.bottom = height.toFloat()

        roundRadius = (height / 2).toFloat()

    }

    private fun buildProgressBarRect() {
        progressBarRect.left = 0F
        progressBarRect.top = 0F
        progressBarRect.right = width.toFloat() * currentProgress
        progressBarRect.bottom = height.toFloat()

        val g = LinearGradient(0f,progressBarRect.bottom,progressBarRect.right,
            progressBarRect.bottom,Color.RED,Color.BLUE,Shader.TileMode.CLAMP)
        progressBarPaint.setShader(g)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)

        drawProgressBar(canvas)
    }

    private fun drawProgressBar(canvas: Canvas) {
        buildProgressBarRect()
        canvas.drawRoundRect(progressBarRect,roundRadius,roundRadius,progressBarPaint)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRoundRect(backGroundRect,roundRadius,roundRadius,backGroundPaint)
    }
}