package com.example.simple3.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView


class MyTextView(context: Context, attrs: AttributeSet?): AppCompatTextView(context, attrs) {

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        Log.i("sun", "layout: 执行")
        super.layout(l, t, r, b)
    }

    override fun onDraw(canvas: Canvas?) {
        Log.i("sun", "onDraw: 执行")
        super.onDraw(canvas)
    }
}