package com.example.simple3.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Scroller
import android.widget.TextView
import androidx.core.view.children
import kotlin.math.abs

/**
 * tab 在一定范围内自由滑动
 * @property isLeftScroll Boolean
 * @property startOffset Int
 * @property endOffset Int
 * @property backToAnimation ValueAnimator?
 * @constructor
 */
class OverHorizontalTabsView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ValueAnimator.AnimatorUpdateListener,
    View.OnClickListener {

    /**
     * Item 之间的间隙
     */
    private var childItemGap = 20

    /**
     * 选择颜色
     */
    private var selectColor = Color.parseColor("#ff0000")

    /**
     * 默认颜色
     */
    private var normalColor = Color.parseColor("#eeeeee")

    /**
     * 最后选择 item 的数组下标
     */
    private var lastSelectItemIndex = 0

    var onTabSelectedFun: ((Int) -> Unit)? = null

    private var mScroller: Scroller = Scroller(context)
    private var mVelocityTracker: VelocityTracker? = null
    private var mMinimumVelocity: Int = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private var mMaximumVelocity: Int = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private var mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    private var lastX = 0f
    private var downX = 0f

    /**
     * 包含左右留白的空间的总内容宽度
     */
    private var contentWidth = -1

    private var isLeftScroll = false

    /**
     * 左留白宽度，留白宽度根据第一个和最后一个 Item 宽度进行计算的
     */
    private var startOffset = 0

    /**
     * 右留白宽度
     */
    private var endOffset = 0

    /**
     * 回弹动画
     */
    private var backToAnimation: ValueAnimator? = null
        get() {
            if (field == null) {
                field = ValueAnimator()
            }
            field?.takeIf { !it.isRunning }?.apply {
                if (isLeftScroll) {
                    setIntValues(scrollX, getMaxScrollRange())
                } else {
                    setIntValues(scrollX, 0)
                }
                duration = 400
                addUpdateListener(this@OverHorizontalTabsView)
            }
            return field
        }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount == 0) return

        startOffset = right / 2 - getChildAt(0).measuredWidth / 2 + paddingLeft

        var childLeft = startOffset
        val childTop = paddingTop

        var lastItemWidth = 0
        var lastItemRight = 0

        // 简单水平布局 Item
        for (i in 0 until childCount) {
            getChildAt(i)?.takeIf { it.visibility != GONE }?.also { child ->
                val childRight = childLeft + child.measuredWidth
                child.layout(childLeft, childTop, childRight, childTop + child.measuredHeight)
                childLeft = childRight + childItemGap

                if (i == childCount - 1){
                    lastItemWidth = child.width
                    lastItemRight = child.right
                }
            }
        }

        endOffset = right / 2 - lastItemWidth / 2

        contentWidth = lastItemRight + endOffset
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 如果 scroller 动画没停止，但是用户已经触摸，则该立刻停止
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation();
                }
                // 停止回弹动画
                if (backToAnimation?.isRunning == true) {
                    backToAnimation?.cancel()
                }
                lastX = event.x
                downX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.x - lastX
                if (abs(moveX) > mTouchSlop) {
                    lastX = event.x
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        initVelocityTracker()
        mVelocityTracker!!.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val move = event.x - lastX

                var realMoveDis = -move
                val preScrollX = scrollX + move
                // 边界和阻尼控制
                if (preScrollX < 0 || abs(preScrollX) > getMaxScrollRange()) { // 左越界,右越界
                    realMoveDis /= 4
                }
                Log.e("sun", "滑动scrollX$scrollX")

                scrollBy(realMoveDis.toInt(), 0)
                lastX = event.x
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 当快速滑动时，使用 scroller 完成流程滑动
                val tracker = mVelocityTracker
                tracker?.apply {
                    this.computeCurrentVelocity(1000,mMaximumVelocity.toFloat())
                    val velocity = -xVelocity.toInt()
                    isLeftScroll = event.x < downX

                    val canFling = (scrollX < getMaxScrollRange()) && (scrollX > 0)
                    if (abs(velocity) > mMinimumVelocity && canFling) {
                        Log.e("sun", "触发fliing")
                        mScroller.fling(
                            scrollX, 0, velocity, 0, 0,
                            getMaxScrollRange(), 0, 0
                        )
                        postInvalidate()
                    } else {
                        Log.e("sun", "${left}${event.x}**${downX}")
                        canGotoBack()
                    }

                    lastX = 0f
                    downX = 0f
                }

                mVelocityTracker?.recycle()
                mVelocityTracker = null
            }
        }
        return true
    }

    private fun initVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun canGotoBack() {
        if (abs(scrollX) > getMaxScrollRange() || scrollX < 0) {
            backToAnimation?.start()
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        val currx = mScroller.currX
        if (mScroller.computeScrollOffset()) {
            Log.e("sun", "继续${mScroller.currX}")
            scrollTo(currx, 0)
            postInvalidate()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        // 处理回弹
        val value = animation!!.animatedValue as Int
        scrollTo(value, 0)
        Log.e("sun", "回弹执行$value")
    }

    private fun getMaxScrollRange(): Int {
        return contentWidth - width
    }

    private fun buildTextItemView(tabTitle: String): TextView {
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textSize = 16f
            text = tabTitle
            setOnClickListener(this@OverHorizontalTabsView)
        }
    }

    override fun onClick(v: View) {
        val itemIndex = children.indexOf(v)
        if (itemIndex == lastSelectItemIndex) return

        changeSelectUi(itemIndex)
        onTabSelectedFun?.invoke(itemIndex)

        lastSelectItemIndex = itemIndex
    }

    private fun changeSelectUi(itemIndex: Int) {
        val lastItem = getChildAt(lastSelectItemIndex) as TextView
        val currentItem = getChildAt(itemIndex) as TextView

        lastItem.setTextColor(normalColor)
        currentItem.setTextColor(selectColor)

        tabSelectChangeScrll(currentItem.width, currentItem.left)
    }

    private fun tabSelectChangeScrll(selectItemWidth: Int, selectItemLeft: Int) {
        Log.e("sun", "位置${selectItemLeft}**${selectItemLeft - scrollX}")
        // 计算已选择 item left
        val itemLeftOfParent = selectItemLeft - scrollX
        // 判断在中线左边还是右边
        val left = itemLeftOfParent < width / 2
        // 根据左右进行滑动
        var scrllDistance = 0
        if (left) {
            // 位于中线右边，需要向右滑动
            scrllDistance = -(width / 2 - (itemLeftOfParent + selectItemWidth / 2))
        } else {
            scrllDistance = itemLeftOfParent - width / 2 + selectItemWidth / 2
        }
        mScroller.startScroll(scrollX, 0, scrllDistance, 0)
        postInvalidate()
    }

    /*****************/
    /**
     * 绑定 tab
     * @param tabs List<String>
     */
    fun bindTabs(tabs: List<String>) {
        if (childCount != 0) {
            removeAllViews()
        }

        tabs.forEachIndexed { index, title ->
            val itemView = buildTextItemView(title).apply {
                if (lastSelectItemIndex == index) {
                    setTextColor(selectColor)
                } else {
                    setTextColor(normalColor)
                }
            }
            addView(itemView)
        }
    }

    /**
     * 设置选择的 tab
     * @param index Int
     */
    fun setSelectTab(index: Int) {
        onClick(getChildAt(index))
    }

}