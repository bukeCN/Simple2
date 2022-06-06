package com.example.simple3.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView


class MyTextView(context: Context, attrs: AttributeSet?): View(context, attrs) {

    init {
        Log.e("sun", "init")
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Log.e("sun", "onFinishInflate")
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Log.e("sun", "onVisibilityChanged$visibility")
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        Log.e("sun", "onWindowVisibilityChanged$visibility")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e("sun", "onAttachedToWindow")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.e("sun", "onLayout")
    }

    override fun onDraw(canvas: Canvas?) {
        Log.i("sun", "onDraw: 执行")
        super.onDraw(canvas)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.e("sun", "onDetachedFromWindow")
    }
}