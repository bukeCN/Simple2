package com.example.simple3.view

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Scroller
import com.example.simple3.R
import com.google.android.material.appbar.AppBarLayout

class ScaleLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ViewTreeObserver.OnGlobalLayoutListener,
    AnimatorUpdateListener {

    private var mScroller: Scroller = Scroller(context)
    private var mVelocityTracker: VelocityTracker? = null
    private var mMaximumVelocity: Int = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private var mMinimumVelocity: Int = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private var mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    var appBarLayout: AppBarLayout? = null
    var target_view: View? = null
    var appBarLayoutHeight = 0

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewTreeObserver.addOnGlobalLayoutListener(this)
        // 获取 AppBarLayout, 添加滑动监听
        appBarLayout = findViewById(R.id.appbar_layout)
        target_view = findViewById(R.id.target_view)
    }

    override fun onGlobalLayout() {
        // 获取 AppBarLayotu 获取高度以便拦截滑动事件
        appBarLayout?.height?.apply { appBarLayoutHeight = this }
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    private var lastY = -1f
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // 如果 scroller 动画没停止，但是用户已经触摸，则该立刻停止
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                lastY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val moveY = ev.y - lastY
                if (moveY > mTouchSlop && appBarLayout?.bottom!! >= appBarLayoutHeight) { // 顶部下滑
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    }
                    lastY = ev.y
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private var moveCount = 0
    override fun onTouchEvent(event: MotionEvent): Boolean {
        postInvalidateOnAnimation()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker!!.addMovement(event);

                var move = event.y - lastY
                moveCount += move.toInt()
                if (moveCount > 100) {
                    move /= 4
                }
                scaleBig()
                scrollBy(0, -move.toInt())
                lastY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 当快速滑动时，使用 scroller 完成流程滑动
                mVelocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat());
                val velocity = mVelocityTracker!!.getYVelocity();
                Log.e("sun", "速度" + velocity + "默认速度" + mMinimumVelocity);

                if (Math.abs(velocity) > mMinimumVelocity) {
                    mScroller.fling(0, getScrollY(), 0, -velocity.toInt(), 0, 0, 0, 300);
                    invalidate()
                } else {
                    getBackToAnimation().start()
                }
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
                lastY = 0f
                moveCount = 0
            }
        }
        return true
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return super.generateLayoutParams(attrs)
    }
    override fun generateDefaultLayoutParams(): LayoutParams {
        return super.generateDefaultLayoutParams()
    }

    private var flingStrat = false
    override fun computeScroll() {
        val curry = mScroller.currY
        Log.e("sun", "位置${mScroller.currY}")
        if (mScroller.computeScrollOffset()) {
            Log.e("sun", "继续${mScroller.currY}")
            scrollTo(0, curry)
//            postInvalidate()
            if (mScroller.currY == 0) {
                flingStrat = false
                Log.e("sun", "完成${mScroller.currY}")
                getBackToAnimation().takeIf { !it.isRunning }?.start()
            }
        } else {
            if (mScroller.isFinished && curry != 0 && flingStrat) {
                flingStrat = false
                Log.e("sun", "完成${mScroller.currY}")
                getBackToAnimation().takeIf { !it.isRunning }?.start()
            }
        }
    }

    private var backToAnimation: ValueAnimator? = null

    fun getBackToAnimation(): ValueAnimator {
        if (backToAnimation == null) {
            backToAnimation = ValueAnimator()
        }
        backToAnimation?.apply {
            setIntValues(scrollY, 0)
            setDuration(200)
            addUpdateListener(this@ScaleLayout)
        }
        return backToAnimation!!
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val value = animation!!.animatedValue as Int
        scrollTo(0, value)
        Log.e("sun", "进度${animation.animatedFraction}")

        //  todo 缩放算法调节
        val s = (scaleValue - 1) * animation.animatedFraction + 1
        target_view?.scaleX = s.toFloat()
        target_view?.scaleY = s.toFloat()
    }

    //  todo 缩放算法调节
    private var scaleValue: Double = 1.toDouble()
    fun scaleBig() {
        scaleValue *= 1.01
        target_view?.scaleX = scaleValue.toFloat()
        target_view?.scaleY = scaleValue.toFloat()
    }
}