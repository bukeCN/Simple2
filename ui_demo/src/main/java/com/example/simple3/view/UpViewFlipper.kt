package com.example.simple3.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.ViewAnimator
import android.widget.ViewFlipper
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

/**
 * 两个 tv ，上下滚动显示布局，可以和 ViewFlipper 一样添加动画效果.
 * 默认不展示但保留位置
 */
class UpViewFlipper(context: Context, attrs: AttributeSet?): ViewAnimator(context, attrs) {
    private val tag = "UpViewFlipper"

    companion object {
        /**
         * 延迟时间
         * 毫秒
         */
        const val DELAY_TIME: Long = 1000L
    }

    /**
     * 承载轮换显示的子 View
     */
    private lateinit var childPair: Pair<TextView,TextView>

    /**
     * 礼物承载
     */
    private val contentQueue: LinkedBlockingDeque<String> = LinkedBlockingDeque()

    /**
     * 是否是首次展示，第一次不要 showNext
     */
    private var isFirstShow = false

    /**
     * 目前展示在前台的 ItemView
     */
    private var usedItemView: TextView? =null

    /**
     * 是不是开始了？
     */
    private var mStarted = false

    /**
     * 正在运行中吗???
     */
    private var mRuning = false

    constructor(context: Context):this(context, null)

    init {
        // 默认不展示
        visibility = View.INVISIBLE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (childCount == 2){
            val first = getChildAt(0) as TextView
            val second = getChildAt(1) as TextView
            childPair = Pair(first, second)
        } else {
            Log.w(tag, "Child View 数量不对")
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
        contentQueue.clear()
    }

    private val nextRunnable = object: Runnable{
        override fun run() {
            val gift = contentQueue.pollFirst()

            gift?.also { // 有礼物，进行展示。
                if (visibility == INVISIBLE){
                    visibility = VISIBLE
                }

                val currentItem = getNextItemView()
                currentItem?.apply {
                    text = gift
                }
                println(gift)
                if (!isFirstShow){
                    showNext()
                }

                // 后续如果还有，则开启自动循环，没有就停止
                if (contentQueue.isNotEmpty()){
                    postDelayed(this, Companion.DELAY_TIME)
                } else {
                    stop()
                }
            } ?: also { // 没有礼物了
                removeCallbacks(this)
            }
        }
    }

    /**
     * 获取用于展示下一条的 View
     */
    private fun getNextItemView(): TextView?{
        if (usedItemView == null){
            usedItemView = childPair.first
            // 标记一下，第一次显示，不要执行 showNext
            isFirstShow = true
        } else {
            isFirstShow = false
            if (usedItemView === childPair.first){
                usedItemView = childPair.second
            } else {
                usedItemView = childPair.first
            }
        }
        return usedItemView
    }

    /**
     * 更新运行状态，不会清空队列
     */
    private fun updateRuning(){
        val isRuning = isAttachedToWindow && mStarted
        if (isRuning != mRuning){
            if (isRuning){
                postDelayed(nextRunnable, Companion.DELAY_TIME)
            } else {
                removeCallbacks(nextRunnable)
            }
            mRuning = isRuning
        }
    }

    private fun start(){
        mStarted = true
        updateRuning()
    }

    private fun stop(){
        mStarted = false
        updateRuning()
    }

    /**********************
     * 对外函数
     **********************
     */
    @Synchronized
    fun addOnceShow(content: String){
        visibility = View.VISIBLE
        contentQueue.put(content)
        start()
    }


}