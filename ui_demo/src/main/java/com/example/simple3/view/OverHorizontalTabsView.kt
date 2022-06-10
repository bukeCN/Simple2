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
import androidx.viewpager2.widget.ViewPager2
import com.example.simple3.R
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
    private var selectColor = Color.BLACK

    /**
     * 默认颜色
     */
    private var normalColor = Color.GRAY

    private var itemTextSize = 16f

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

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.OverHorizontalTabsView, 0, 0
        )

        selectColor =
            typedArray.getColor(R.styleable.OverHorizontalTabsView_selectedColor, Color.BLACK)
        normalColor =
            typedArray.getColor(R.styleable.OverHorizontalTabsView_normalColor, Color.GRAY)
        itemTextSize = typedArray.getDimension(R.styleable.OverHorizontalTabsView_itemTextSize, 16f)

        typedArray.recycle()
    }

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

                if (i == childCount - 1) {
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

                scrollBy(realMoveDis.toInt(), 0)
                lastX = event.x
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 当快速滑动时，使用 scroller 完成流程滑动
                val tracker = mVelocityTracker
                tracker?.apply {
                    this.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val velocity = -xVelocity.toInt()
                    isLeftScroll = event.x < downX

                    val canFling = (scrollX < getMaxScrollRange()) && (scrollX > 0)
                    if (abs(velocity) > mMinimumVelocity && canFling) {
                        Log.e("sun", "触发 Fling")

                        mScroller.fling(
                            scrollX, 0, velocity, 0, 0,
                            getMaxScrollRange(), 0, 0
                        )
                        postInvalidate()
                    } else {
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
        Log.e("sun", "当前值：$currx")

        if (mScroller.computeScrollOffset()) {
            Log.e("sun", "Fling：$currx")
            val oldX: Int = scrollX
            val x = mScroller.currX
            if (x != oldX) {
                scrollBy(x - oldX, 0)
                postInvalidate()
            }
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        // 处理回弹
        val value = animation!!.animatedValue as Int
        Log.e("sun", "回弹执行：$value")
        scrollTo(value, 0)
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
            textSize = itemTextSize
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

    var startFlowViewPager2Rate = -1f

    var isFlowFling = false

    var isOnceScrollDs = 0

    /**
     * 跟随 ViewPager2 滑动
     * @param position Int
     * @param positionOffset Float
     * @param positionOffsetPixels Int
     */
    fun flowOnViewPager2(
        viewPager2: ViewPager2
    ) {
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
//                Log.e(
//                    "sunn",
//                    "傻叉${position}**${positionOffset}**${positionOffset * 108}**${positionOffsetPixels}"
//                )
//                val left = startFlowViewPager2Rate < 0.5
//                val nextItemIndex = if (left) position + 1 else position - 1
//                val currentItemLeft = getChildAt(lastSelectItemIndex).left
//                if (nextItemIndex < 0 || nextItemIndex > childCount - 1) return
//                getChildAt(nextItemIndex)?.also { nextItem ->
//                    val maxDistance = nextItem.left - currentItemLeft
//////        val needDistance = (positionOffset * 100) * (maxDistance * 100) / 100
//////        Log.e("sun","滑动_我擦：${(positionOffset * 100)}***${(maxDistance * 100)}")
//////        val realDistance = if (left) needDistance else -needDistance
//////        Log.e("sun","滑动：${maxDistance}***${needDistance}***$realDistance")
//////        // 4. 滑动
//                    scrollTo((positionOffset * maxDistance).toInt() + isOnceScrollDs, 0)
//                }

//                it.flowOnViewPager2(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                setSelectTab(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("sunn", "状态:$state")
                if (state == 1) {
                    // 判断在中线左边还是右边
                    val currentItem = getChildAt(lastSelectItemIndex)

                    // 计算已选择 item left
                    val itemLeftOfParent = currentItem.left - scrollX
                    // 判断在中线左边还是右边
                    val left = itemLeftOfParent < width / 2
                    // 根据左右进行滑动
                    var scrllDistance = 0
                    if (left) {
                        // 位于中线右边，需要向右滑动
                        scrllDistance = -(width / 2 - (itemLeftOfParent + currentItem.width / 2))
                    } else {
                        scrllDistance = itemLeftOfParent - width / 2 + currentItem.width / 2
                    }
                    mScroller.startScroll(scrollX, 0, scrllDistance, 0, 0)
                    postInvalidate()
                }
                //                    val currentItemLeft = currentItem.left
//                    val currentItemWidth = currentItem.width
//
//                    // 计算已选择 item left
//                    val itemLeftOfParent = currentItemLeft - scrollX
//                    val left = itemLeftOfParent < width / 2
//
//                    // 2.5 确定当前位置 Item 是否居中，不居中飞回来，并且添加已滑动的值
//                    val currentItemMiddlePostion =
//                        currentItem.left - scrollX + (currentItem.width / 2)
//                    val isCenter = currentItemMiddlePostion == width / 2
//
//                    if (!isCenter && !isFlowFling) {
//                        isFlowFling = true
//                        // 2.5.1 确定飞回来的滑动距离
//                        // 计算已选择 item left
//                        val itemLeftOfParent = currentItemLeft - scrollX
//                        var scrllDistance = 0
//                        if (left) {
//                            // 位于中线右边，需要向右滑动
//                            scrllDistance = -(width / 2 - (itemLeftOfParent + currentItemWidth / 2))
//                        } else {
//                            scrllDistance = itemLeftOfParent - width / 2 + currentItemWidth / 2
//                        }
//                        scrollBy(scrllDistance, 0)
//                        return
//                    }
//                }
            }
        })
//
//        // 1. 确定用户手指滑动方向
//        if (startFlowViewPager2Rate == -1f) {
//            startFlowViewPager2Rate = positionOffset
//            isOnceScrollDs = scrollX
//        }
//        val left = startFlowViewPager2Rate < 0.5
//        // 2. 确定当前 Item 位置
//        val nextItemIndex = if (left) position + 1 else position - 1
//
//        val currentItem = getChildAt(position)
//        val currentItemLeft = currentItem.left
//        val currentItemWidth = currentItem.width
//
//        // 2.5 确定当前位置 Item 是否居中，不居中飞回来，并且添加已滑动的值
//        val currentItemMiddlePostion = currentItem.left - scrollX + (currentItem.width / 2)
//        val isCenter = currentItemMiddlePostion == width / 2
//
//        if (!isCenter && !isFlowFling) {
//            isFlowFling = true
//            // 2.5.1 确定飞回来的滑动距离
//            // 计算已选择 item left
//            val itemLeftOfParent = currentItemLeft - scrollX
//            var scrllDistance = 0
//            if (left) {
//                // 位于中线右边，需要向右滑动
//                scrllDistance = -(width / 2 - (itemLeftOfParent + currentItemWidth / 2))
//            } else {
//                scrllDistance = itemLeftOfParent - width / 2 + currentItemWidth / 2
//            }
//            scrollBy(scrllDistance, 0)
//            return
//        }

//        // 3. 确定最大滑动距离，计算需要滑动的值(注意正负方向)
//        getChildAt(nextItemIndex)?.also { nextItem ->
//            val maxDistance = nextItem.left - currentItemLeft
////        val needDistance = (positionOffset * 100) * (maxDistance * 100) / 100
////        Log.e("sun","滑动_我擦：${(positionOffset * 100)}***${(maxDistance * 100)}")
////        val realDistance = if (left) needDistance else -needDistance
////        Log.e("sun","滑动：${maxDistance}***${needDistance}***$realDistance")
////        // 4. 滑动
//            scrollTo((positionOffset * maxDistance).toInt() + isOnceScrollDs, 0)
////        isOnceScrollDs += needDistance
////
////        // end
//        if (positionOffset == 0f && positionOffsetPixels == 0) {
//            startFlowViewPager2Rate = -1f
//            isFlowFling = false
////            isOnceScrollDs = 0f
//        }
//        }
    }
}
