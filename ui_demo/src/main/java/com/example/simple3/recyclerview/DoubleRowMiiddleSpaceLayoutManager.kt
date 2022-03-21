package com.example.simple3.recyclerview

import android.icu.util.Measure
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

/**
 * 两行两列，中间空白区域的 RecyclerView LayoutManager, 支持滑动，
 * 仅仅派对心动玩法前三个阶段使用
 * 1 2 * 5 6 * 9 10
 * 3 4 * 7 8 * 11 12
 */
class DoubleRowMiiddleSpaceLayoutManager : RecyclerView.LayoutManager() {
    /**
     * 两列中间留白的宽度
     */
    private var spaceWidth = 0

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        Log.e("sun","onLayoutChildren")

        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
        }
        if (itemCount == 0 && state.isPreLayout) return
        detachAndScrapAttachedViews(recycler)

        fill(recycler, state)
    }

    private fun fill(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val childWidth = getChildWidth()

        var topOffset = paddingTop
        var leftOffset = paddingLeft

        // 列数统计, 从 0 开始
        var countRow = 0

        // 使用场景唯一，直接遍历全部子 View
        for (i in 0 until itemCount) {
            val child = recycler.getViewForPosition(i)

            // 确定子 View 尺寸
            child.layoutParams.width = childWidth
            addView(child)
            // 安排测量
            measureChildWithMargins(child, 0, 0)

            // 获取 child 尺寸
            val horizontalSpace = getDecoratedMeasurementHorizontal(child)
            val verticalSpace = getDecoratedMeasurementVertical(child)

            Log.e("sun", "$horizontalSpace***$verticalSpace")

            // 放置子 View
            layoutDecoratedWithMargins(
                child, leftOffset, topOffset,
                leftOffset + horizontalSpace, topOffset + verticalSpace
            )

            // 计算下一个 left 和 top
            if (isNeedAessertSwitchRow(i)) {
                if (isSwitchRow(i)) {
                    // 加列
                    countRow++

                    leftOffset = countRow * (horizontalSpace * 2) + spaceWidth// 添加中间留白空间
                    topOffset = paddingTop
                } else {
                    // 换行
                    leftOffset = countRow * (horizontalSpace * 2)
                    if (countRow > 0){
                        leftOffset += spaceWidth
                    }
                    topOffset += verticalSpace
                }
            } else {
                leftOffset += horizontalSpace
            }
        }
    }

    /**
     * 根据 RV 的宽度计算子view宽度
     */
    private fun getChildWidth(): Int {
        var childWidth = width / 5
        spaceWidth = (childWidth * 0.8).toInt()
        childWidth = (width - spaceWidth) / 4
        return childWidth
    }

    /**
     * 是否需要进入判断换行还是换列逻辑
     * true yes
     * false no
     */
    private fun isNeedAessertSwitchRow(index: Int): Boolean {
        if (index == 0) return false
        return index % 2 != 0
    }

    /**
     * 判断换行还是换列
     * false 换行
     * true 换列
     * 1 2 * 5 6 * 9 10
     * 3 4 * 7 8 * 11 12
     */
    private fun isSwitchRow(index: Int): Boolean {
        val value = index + 1
        val result = (value / 2) % 2
        return result == 0
    }

    /**
     * 获取 view 水平占据的空间
     */
    private fun getDecoratedMeasurementHorizontal(view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin
    }

    /**
     * 获取 view 垂直占据的空间
     */
    private fun getDecoratedMeasurementVertical(view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin
    }


    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}