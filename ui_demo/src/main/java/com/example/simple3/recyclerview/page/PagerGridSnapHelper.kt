package com.example.simple3.recyclerview.page

import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider

import androidx.recyclerview.widget.SnapHelper
import com.example.simple3.recyclerview.page.PagerConfig.getFlingThreshold
import java.lang.Exception
import java.lang.IllegalStateException


class PagerGridSnapHelper : SnapHelper() {
    private var mRecyclerView // RecyclerView
            : RecyclerView? = null

    /**
     * 用于将滚动工具和 Recycler 绑定
     *
     * @param recyclerView RecyclerView
     * @throws IllegalStateException 状态异常
     */
    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(@Nullable recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    /**
     * 计算需要滚动的向量，用于页面自动回滚对齐
     *
     * @param layoutManager 布局管理器
     * @param targetView    目标控件
     * @return 需要滚动的距离
     */
    @Nullable
    override fun calculateDistanceToFinalSnap(
        @NonNull layoutManager: RecyclerView.LayoutManager,
        @NonNull targetView: View
    ): IntArray {
        val pos = layoutManager.getPosition(targetView)
        var offset = IntArray(2)
        if (layoutManager is PagerGridLayoutManager) {
            offset = layoutManager.getSnapOffset(pos)
        }
        return offset
    }

    /**
     * 获得需要对齐的View，对于分页布局来说，就是页面第一个
     *
     * @param layoutManager 布局管理器
     * @return 目标控件
     */
    @Nullable
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager is PagerGridLayoutManager) {
            return layoutManager.findSnapView()
        }
        return null
    }

    /**
     * 获取目标控件的位置下标
     * (获取滚动后第一个View的下标)
     *
     * @param layoutManager 布局管理器
     * @param velocityX     X 轴滚动速率
     * @param velocityY     Y 轴滚动速率
     * @return 目标控件的下标
     */
    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int, velocityY: Int
    ): Int {
        var target = RecyclerView.NO_POSITION
        if (null != layoutManager && layoutManager is PagerGridLayoutManager) {
            val manager = layoutManager
            if (manager.canScrollHorizontally()) {
                if (velocityX > getFlingThreshold()) {
                    target = manager.findNextPageFirstPos()
                } else if (velocityX < -getFlingThreshold()) {
                    target = manager.findPrePageFirstPos()
                }
            } else if (manager.canScrollVertically()) {
                if (velocityY > getFlingThreshold()) {
                    target = manager.findNextPageFirstPos()
                } else if (velocityY < -getFlingThreshold()) {
                    target = manager.findPrePageFirstPos()
                }
            }
        }
        return target
    }

    /**
     * 一扔(快速滚动)
     *
     * @param velocityX X 轴滚动速率
     * @param velocityY Y 轴滚动速率
     * @return 是否消费该事件
     */
    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        val layoutManager = mRecyclerView!!.layoutManager ?: return false
        val adapter = mRecyclerView!!.adapter ?: return false
        val minFlingVelocity = getFlingThreshold()
        return ((Math.abs(velocityY) > minFlingVelocity || Math.abs(velocityX) > minFlingVelocity)
                && snapFromFling(layoutManager, velocityX, velocityY))
    }

    /**
     * 快速滚动的具体处理方案
     *
     * @param layoutManager 布局管理器
     * @param velocityX     X 轴滚动速率
     * @param velocityY     Y 轴滚动速率
     * @return 是否消费该事件
     */
    private fun snapFromFling(
        @NonNull layoutManager: RecyclerView.LayoutManager, velocityX: Int,
        velocityY: Int
    ): Boolean {
        if (layoutManager !is ScrollVectorProvider) {
            return false
        }
        val smoothScroller = createSnapScroller(layoutManager) ?: return false
        val targetPosition = findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false
        }
        smoothScroller.targetPosition = targetPosition
        layoutManager.startSmoothScroll(smoothScroller)
        return true
    }

    /**
     * 通过自定义 LinearSmoothScroller 来控制速度
     *
     * @param layoutManager 布局故哪里去
     * @return 自定义 LinearSmoothScroller
     */
    override fun createSnapScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller? {
        return if (layoutManager !is ScrollVectorProvider) {
            null
        } else PagerGridSmoothScroller(mRecyclerView!!)
    }
    //--- 公开方法 ----------------------------------------------------------------------------------
    /**
     * 设置滚动阀值
     * @param threshold 滚动阀值
     */
    fun setFlingThreshold(threshold: Int) {
        PagerConfig.setFlingThreshold(threshold)
    }
}