package com.example.simple3.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class GSampleView(context: Context, attrs: AttributeSet?) :
        ConstraintLayout(context, attrs) {

    private lateinit var firstChild: View

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        firstChild = getChildAt(0)
    }

    private var downX: Float = 0f
    private var downY: Float = 0f

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                downX = ev.x
                downY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = ev.x - downX
                if (moveX < 0) {
                    // 左滑
                    // 询问子 View 是否要左滑，要就请父 View 不要拦，否则就让父 View 拦截
                    parent.requestDisallowInterceptTouchEvent(RecyclerViewActivity.FLAG == 1)
                } else {
                    // 右滑
                    parent.requestDisallowInterceptTouchEvent(RecyclerViewActivity.FLAG == -1)
                }
            }
        }
        val result = super.dispatchTouchEvent(ev)
        Log.e("sun", "dispatchTouchEvent: 结果${result}")
        return result
    }
}