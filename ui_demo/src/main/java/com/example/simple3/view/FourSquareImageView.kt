package com.example.simple3.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.simple3.R

/**
 * 最多 4 个 img 重叠的 img 组合 View
 * 业务场景固定，不适合通用
 * 1 大小为 48x48
 * 2 68x40
 * 3、4 60x60 3 为倒三角
 */
class FourSquareImageView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    // 子 view 之间的间隔
    private var childGap = 8.toPx()

    // 子 View 大小？父 View 的一半 + 间隙的一半
    private var viewWidth = 0
    private var viewHeigt = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 强制改变大小
        val realeWidthSpec = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY)
        val realeHeightSpec = MeasureSpec.makeMeasureSpec(viewHeigt, MeasureSpec.EXACTLY)
        super.onMeasure(realeWidthSpec, realeHeightSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var usedWidth = 0
        var usedHeight = 0

        for (index in 0 until childCount) {
            val child = getChildAt(index)
            when (childCount) {
                1 -> {
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
                2 -> {
                    val childLeft = usedWidth
                    val childTop = usedHeight
                    val childRight = childLeft + child.measuredWidth
                    val childBottom = childTop + child.measuredHeight

                    child.layout(childLeft, childTop, childRight, childBottom)

                    usedWidth = child.measuredWidth - childGap
                }
                3, 4 -> {
                    val childLeft = usedWidth
                    val childTop = usedHeight
                    val childRight = childLeft + child.measuredWidth
                    val childBottom = childTop + child.measuredHeight

                    child.layout(childLeft, childTop, childRight, childBottom)

                    usedWidth += child.measuredWidth - childGap
                    // 下一次是否需要换行
                    if (usedWidth >  viewWidth / 2) {
                        // 换行处理
                        if (childCount == 3) {// 区别 3，4 两种情况
                            usedWidth = viewWidth / 2 - child.measuredWidth / 2
                        } else {
                            usedWidth = 0
                        }
                        usedHeight = viewHeigt / 2 - childGap / 2
                    }
                }
            }
        }
    }

    fun setImages(src: List<String>) {
        removeAllViews()
        var size = 0
        when (src.size) {
            0 -> {
                viewWidth = 48.toPx()
                viewHeigt = 48.toPx()
            }
            1 -> {
                viewWidth = 48.toPx()
                viewHeigt = 48.toPx()

                size = viewWidth
            }
            2 -> {
                viewWidth = 68.toPx()
                viewHeigt = 40.toPx()

                size = viewHeigt
                childGap = 12.toPx()
            }
            else -> {
                viewWidth = 60.toPx()
                viewHeigt = 60.toPx()

                size = viewWidth/2 + 8.toPx() / 2
                childGap = 8.toPx()
            }
        }

        // 添加 View
        src.forEachIndexed { index, url ->
            if (index < 4) {
                val imgView = ImageView(context).also { img ->
                    img.layoutParams =
                        LayoutParams(size, size)
                }
                imgView.setImageResource(R.mipmap.ic_launcher_round)
                addView(imgView)
            }
        }
    }
}