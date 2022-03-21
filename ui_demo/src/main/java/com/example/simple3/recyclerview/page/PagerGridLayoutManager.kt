package com.example.simple3.recyclerview.page

import androidx.recyclerview.widget.LinearSmoothScroller

import androidx.recyclerview.widget.RecyclerView

import android.graphics.PointF

import android.view.ViewGroup

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log

import android.util.SparseArray
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider


class PagerGridLayoutManager(rows: Int,columns: Int,
    @OrientationType orientation: Int
) :
    RecyclerView.LayoutManager(), ScrollVectorProvider {
    @IntDef(*[VERTICAL, HORIZONTAL])
    annotation class OrientationType  // 滚动类型

    @OrientationType
    private var mOrientation // 默认水平滚动
            : Int

    /**
     * 获取当前 X 轴偏移量
     *
     * @return X 轴偏移量
     */
    var offsetX = 0 // 水平滚动距离(偏移量)
        private set

    /**
     * 获取当前 Y 轴偏移量
     *
     * @return Y 轴偏移量
     */
    var offsetY = 0 // 垂直滚动距离(偏移量)
        private set
    private val mRows // 行数
            : Int
    private val mColumns // 列数
            : Int
    private val mOnePageSize // 一页的条目数量
            : Int
    private val mItemFrames // 条目的显示区域
            : SparseArray<Rect>
    private var mItemWidth = 0 // 条目宽度
    private var mItemHeight = 0 // 条目高度
    private var mWidthUsed = 0 // 已经使用空间，用于测量View
    private var mHeightUsed = 0 // 已经使用空间，用于测量View
    private var mMaxScrollX // 最大允许滑动的宽度
            = 0
    private var mMaxScrollY // 最大允许滑动的高度
            = 0
    private var mScrollState: Int = SCROLL_STATE_IDLE // 滚动状态
    /**
     * 是否允许连续滚动，默认为允许
     *
     * @return true 允许， false 不允许
     */
    /**
     * 设置是否允许连续滚动
     *
     * @param allowContinuousScroll true 允许，false 不允许
     */
    var isAllowContinuousScroll = true // 是否允许连续滚动
    private var mRecyclerView: RecyclerView? = null
    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        mRecyclerView = view
    }
    //--- 处理布局 ----------------------------------------------------------------------------------
    /**
     * 布局子View
     *
     * @param recycler Recycler
     * @param state    State
     */
    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {

        // 如果是 preLayout 则不重新布局
        if (state.isPreLayout || !state.didStructureChange()) {
            return
        }
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            // 页面变化回调
            setPageCount(0)
            setPageIndex(0, false)
            return
        } else {
            setPageCount(totalPageCount)
            setPageIndex(pageIndexByOffset, false)
        }

        // 计算页面数量
        var mPageCount = itemCount / mOnePageSize
        if (itemCount % mOnePageSize != 0) {
            mPageCount++
        }

        // 计算可以滚动的最大数值，并对滚动距离进行修正
        if (canScrollHorizontally()) {
            mMaxScrollX = (mPageCount - 1) * usableWidth
            mMaxScrollY = 0
            if (offsetX > mMaxScrollX) {
                offsetX = mMaxScrollX
            }
        } else {
            mMaxScrollX = 0
            mMaxScrollY = (mPageCount - 1) * usableHeight
            if (offsetY > mMaxScrollY) {
                offsetY = mMaxScrollY
            }
        }

        // 接口回调
        // setPageCount(mPageCount);
        // setPageIndex(mCurrentPageIndex, false);
        if (mItemWidth <= 0) {
            mItemWidth = usableWidth / mColumns
        }
        if (mItemHeight <= 0) {
            mItemHeight = usableHeight / mRows
        }
        mWidthUsed = usableWidth - mItemWidth
        mHeightUsed = usableHeight - mItemHeight

        // 预存储两页的View显示区域
        for (i in 0 until mOnePageSize * 2) {
            getItemFrameByPosition(i)
        }
        if (offsetX == 0 && offsetY == 0) {
            // 预存储View
            for (i in 0 until mOnePageSize) {
                if (i >= itemCount) break // 防止数据过少时导致数组越界异常
                val view: View = recycler.getViewForPosition(i)
                addView(view)
                measureChildWithMargins(view, mWidthUsed, mHeightUsed)
            }
        }

        // 回收和填充布局
        recycleAndFillItems(recycler, state, true)
    }

    /**
     * 布局结束
     *
     * @param state State
     */
    override fun onLayoutCompleted(state: RecyclerView.State) {
        super.onLayoutCompleted(state)
        if (state.isPreLayout) return
        // 页面状态回调
        setPageCount(totalPageCount)
        setPageIndex(pageIndexByOffset, false)
    }

    /**
     * 回收和填充布局
     *
     * @param recycler Recycler
     * @param state    State
     * @param isStart  是否从头开始，用于控制View遍历方向，true 为从头到尾，false 为从尾到头
     */
    @SuppressLint("CheckResult")
    private fun recycleAndFillItems(
        recycler: Recycler, state: RecyclerView.State,
        isStart: Boolean
    ) {
        if (state.isPreLayout) {
            return
        }

        // 计算显示区域区前后多存储一列或则一行
        val displayRect = Rect(
            offsetX - mItemWidth, offsetY - mItemHeight,
            usableWidth + offsetX + mItemWidth, usableHeight + offsetY + mItemHeight
        )
        // 对显显示区域进行修正(计算当前显示区域和最大显示区域对交集)
        displayRect.intersect(0, 0, mMaxScrollX + usableWidth, mMaxScrollY + usableHeight)
        var startPos = 0 // 获取第一个条目的Pos
        val pageIndex = pageIndexByOffset
        startPos = pageIndex * mOnePageSize
        startPos = startPos - mOnePageSize * 2
        if (startPos < 0) {
            startPos = 0
        }
        var stopPos = startPos + mOnePageSize * 4
        if (stopPos > itemCount) {
            stopPos = itemCount
        }
        detachAndScrapAttachedViews(recycler) // 移除所有View
        if (isStart) {
            for (i in startPos until stopPos) {
                addOrRemove(recycler, displayRect, i)
            }
        } else {
            for (i in stopPos - 1 downTo startPos) {
                addOrRemove(recycler, displayRect, i)
            }
        }
    }

    /**
     * 添加或者移除条目
     *
     * @param recycler    RecyclerView
     * @param displayRect 显示区域
     * @param i           条目下标
     */
    private fun addOrRemove(recycler: Recycler, displayRect: Rect, i: Int) {
        val child: View = recycler.getViewForPosition(i)
        val rect: Rect = getItemFrameByPosition(i)
        if (!Rect.intersects(displayRect, rect)) {
            removeAndRecycleView(child, recycler) // 回收入暂存区
        } else {
            addView(child)
            measureChildWithMargins(child, mWidthUsed, mHeightUsed)
            val lp = child.getLayoutParams() as RecyclerView.LayoutParams
            layoutDecorated(
                child,
                rect.left - offsetX + lp.leftMargin + paddingLeft,
                rect.top - offsetY + lp.topMargin + paddingTop,
                rect.right - offsetX - lp.rightMargin + paddingLeft,
                rect.bottom - offsetY - lp.bottomMargin + paddingTop
            )
        }
    }
    //--- 处理滚动 ----------------------------------------------------------------------------------
    /**
     * 水平滚动
     *
     * @param dx       滚动距离
     * @param recycler 回收器
     * @param state    滚动状态
     * @return 实际滚动距离
     */
    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val newX = offsetX + dx
        var result = dx
        if (newX > mMaxScrollX) {
            result = mMaxScrollX - offsetX
        } else if (newX < 0) {
            result = 0 - offsetX
        }
        offsetX += result
        setPageIndex(pageIndexByOffset, true)
        offsetChildrenHorizontal(-result)
        if (result > 0) {
            recycleAndFillItems(recycler, state, true)
        } else {
            recycleAndFillItems(recycler, state, false)
        }
        return result
    }

    /**
     * 垂直滚动
     *
     * @param dy       滚动距离
     * @param recycler 回收器
     * @param state    滚动状态
     * @return 实际滚动距离
     */
    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val newY = offsetY + dy
        var result = dy
        if (newY > mMaxScrollY) {
            result = mMaxScrollY - offsetY
        } else if (newY < 0) {
            result = 0 - offsetY
        }
        offsetY += result
        setPageIndex(pageIndexByOffset, true)
        offsetChildrenVertical(-result)
        if (result > 0) {
            recycleAndFillItems(recycler, state, true)
        } else {
            recycleAndFillItems(recycler, state, false)
        }
        return result
    }

    /**
     * 监听滚动状态，滚动结束后通知当前选中的页面
     *
     * @param state 滚动状态
     */
    override fun onScrollStateChanged(state: Int) {
        mScrollState = state
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            setPageIndex(pageIndexByOffset, false)
        }
    }
    //--- 私有方法 ----------------------------------------------------------------------------------
    /**
     * 获取条目显示区域
     *
     * @param pos 位置下标
     * @return 显示区域
     */
    private fun getItemFrameByPosition(pos: Int): Rect {
        var rect: Rect? = mItemFrames[pos]
        if (null == rect) {
            rect = Rect()
            // 计算显示区域 Rect

            // 1. 获取当前View所在页数
            val page = pos / mOnePageSize

            // 2. 计算当前页数左上角的总偏移量
            var offsetX = 0
            var offsetY = 0
            if (canScrollHorizontally()) {
                offsetX += usableWidth * page
            } else {
                offsetY += usableHeight * page
            }

            // 3. 根据在当前页面中的位置确定具体偏移量
            val pagePos = pos % mOnePageSize // 在当前页面中是第几个
            val row = pagePos / mColumns // 获取所在行
            val col = pagePos - row * mColumns // 获取所在列
            offsetX += col * mItemWidth
            offsetY += row * mItemHeight

            // 状态输出，用于调试
//            Logi("pagePos = $pagePos")
//            Logi("行 = $row")
//            Logi("列 = $col")
//            Logi("offsetX = $offsetX")
//            Logi("offsetY = $offsetY")
            rect.left = offsetX
            rect.top = offsetY
            rect.right = offsetX + mItemWidth
            rect.bottom = offsetY + mItemHeight

            // 存储
            mItemFrames.put(pos, rect)
        }
        return rect
    }

    /**
     * 获取可用的宽度
     *
     * @return 宽度 - padding
     */
    private val usableWidth: Int
        private get() = width - paddingLeft - paddingRight

    /**
     * 获取可用的高度
     *
     * @return 高度 - padding
     */
    private val usableHeight: Int
        private get() = height - paddingTop - paddingBottom
    //--- 页面相关(私有) -----------------------------------------------------------------------------
    /**
     * 获取总页数
     */
    private val totalPageCount: Int
        private get() {
            if (itemCount <= 0) return 0
            var totalCount = itemCount / mOnePageSize
            if (itemCount % mOnePageSize != 0) {
                totalCount++
            }
            return totalCount
        }

    /**
     * 根据pos，获取该View所在的页面
     *
     * @param pos position
     * @return 页面的页码
     */
    private fun getPageIndexByPos(pos: Int): Int {
        return pos / mOnePageSize
    }

    /**
     * 根据 offset 获取页面Index
     *
     * @return 页面 Index
     */
    private val pageIndexByOffset: Int
        private get() {
            var pageIndex: Int
            if (canScrollVertically()) {
                val pageHeight = usableHeight
                if (offsetY <= 0 || pageHeight <= 0) {
                    pageIndex = 0
                } else {
                    pageIndex = offsetY / pageHeight
                    if (offsetY % pageHeight > pageHeight / 2) {
                        pageIndex++
                    }
                }
            } else {
                val pageWidth = usableWidth
                if (offsetX <= 0 || pageWidth <= 0) {
                    pageIndex = 0
                } else {
                    pageIndex = offsetX / pageWidth
                    if (offsetX % pageWidth > pageWidth / 2) {
                        pageIndex++
                    }
                }
            }
//            Logi("getPageIndexByOffset pageIndex = $pageIndex")
            return pageIndex
        }
    //--- 公开方法 ----------------------------------------------------------------------------------
    /**
     * 创建默认布局参数
     *
     * @return 默认布局参数
     */
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * 处理测量逻辑
     *
     * @param recycler          RecyclerView
     * @param state             状态
     * @param widthMeasureSpec  宽度属性
     * @param heightMeasureSpec 高估属性
     */
    override fun onMeasure(
        recycler: Recycler,
        state: RecyclerView.State,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(recycler, state, widthMeasureSpec, heightMeasureSpec)
        val widthsize: Int = View.MeasureSpec.getSize(widthMeasureSpec) //取出宽度的确切数值
        var widthmode: Int = View.MeasureSpec.getMode(widthMeasureSpec) //取出宽度的测量模式
        val heightsize: Int = View.MeasureSpec.getSize(heightMeasureSpec) //取出高度的确切数值
        var heightmode: Int = View.MeasureSpec.getMode(heightMeasureSpec) //取出高度的测量模式

        // 将 wrap_content 转换为 match_parent
        if (widthmode != EXACTLY && widthsize > 0) {
            widthmode = EXACTLY
        }
        if (heightmode != EXACTLY && heightsize > 0) {
            heightmode = EXACTLY
        }
        setMeasuredDimension(
            View.MeasureSpec.makeMeasureSpec(widthsize, widthmode),
            View.MeasureSpec.makeMeasureSpec(heightsize, heightmode)
        )
    }

    /**
     * 返回滚动的偏移量
     * @param state State
     * @return Int
     */
    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return offsetX
    }

    /**
     * 根据 View.canScrollHorizontally() 函数来看，这里可以返回 0
     * @param state State
     * @return Int
     */
    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
        return 0
    }

    /**
     * 返回可滚动的最大值
     * @param state State
     * @return Int
     */
    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return mMaxScrollX
    }

    /**
     * 是否可以水平滚动
     *
     * @return true 是，false 不是。
     */
    override fun canScrollHorizontally(): Boolean {
        return mOrientation == HORIZONTAL
    }

    /**
     * 是否可以垂直滚动
     *
     * @return true 是，false 不是。
     */
    override fun canScrollVertically(): Boolean {
        return mOrientation == VERTICAL
    }

    /**
     * 找到下一页第一个条目的位置
     *
     * @return 第一个搞条目的位置
     */
    fun findNextPageFirstPos(): Int {
        var page = mLastPageIndex
        page++
        if (page >= totalPageCount) {
            page = totalPageCount - 1
        }
//        Log.e("computeScrollVectorForPosition next = $page")
        return page * mOnePageSize
    }

    /**
     * 找到上一页的第一个条目的位置
     *
     * @return 第一个条目的位置
     */
    fun findPrePageFirstPos(): Int {
        // 在获取时由于前一页的View预加载出来了，所以获取到的直接就是前一页
        var page = mLastPageIndex
        page--
//        Log.e("computeScrollVectorForPosition pre = $page")
        if (page < 0) {
            page = 0
        }
//        Log.e("computeScrollVectorForPosition pre = $page")
        return page * mOnePageSize
    }
    //--- 页面对齐 ----------------------------------------------------------------------------------
    /**
     * 计算到目标位置需要滚动的距离[RecyclerView.SmoothScroller.ScrollVectorProvider]
     *
     * @param targetPosition 目标控件
     * @return 需要滚动的距离
     */
    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val vector = PointF()
        val pos = getSnapOffset(targetPosition)
        vector.x = pos[0].toFloat()
        vector.y = pos[1].toFloat()
        return vector
    }

    /**
     * 获取偏移量(为PagerGridSnapHelper准备)
     * 用于分页滚动，确定需要滚动的距离。
     * [PagerGridSnapHelper]
     *
     * @param targetPosition 条目下标
     */
    fun getSnapOffset(targetPosition: Int): IntArray {
        val offset = IntArray(2)
        val pos = getPageLeftTopByPosition(targetPosition)
        offset[0] = pos[0] - offsetX
        offset[1] = pos[1] - offsetY
        return offset
    }

    /**
     * 根据条目下标获取该条目所在页面的左上角位置
     *
     * @param pos 条目下标
     * @return 左上角位置
     */
    private fun getPageLeftTopByPosition(pos: Int): IntArray {
        val leftTop = IntArray(2)
        val page = getPageIndexByPos(pos)
        if (canScrollHorizontally()) {
            leftTop[0] = page * usableWidth
            leftTop[1] = 0
        } else {
            leftTop[0] = 0
            leftTop[1] = page * usableHeight
        }
        return leftTop
    }

    /**
     * 获取需要对齐的View
     *
     * @return 需要对齐的View
     */
    fun findSnapView(): View? {
        if (null != focusedChild) {
            return focusedChild
        }
        if (childCount <= 0) {
            return null
        }
        val targetPos = pageIndexByOffset * mOnePageSize // 目标Pos
        for (i in 0 until childCount) {
            val childPos = getPosition(getChildAt(i)!!)
            if (childPos == targetPos) {
                return getChildAt(i)
            }
        }
        return getChildAt(0)
    }

    //--- 处理页码变化 -------------------------------------------------------------------------------
    private var mChangeSelectInScrolling = true // 是否在滚动过程中对页面变化回调
    private var mLastPageCount = -1 // 上次页面总数
    private var mLastPageIndex = -1 // 上次页面下标

    /**
     * 设置页面总数
     *
     * @param pageCount 页面总数
     */
    private fun setPageCount(pageCount: Int) {
        if (pageCount >= 0) {
            if (mPageListener != null && pageCount != mLastPageCount) {
                mPageListener!!.onPageSizeChanged(pageCount)
            }
            mLastPageCount = pageCount
        }
    }

    /**
     * 设置当前选中页面
     *
     * @param pageIndex   页面下标
     * @param isScrolling 是否处于滚动状态
     */
    private fun setPageIndex(pageIndex: Int, isScrolling: Boolean) {
//        Log.e("setPageIndex = $pageIndex:$isScrolling")
        if (pageIndex == mLastPageIndex) return
        // 如果允许连续滚动，那么在滚动过程中就会更新页码记录
        if (isAllowContinuousScroll) {
            mLastPageIndex = pageIndex
        } else {
            // 否则，只有等滚动停下时才会更新页码记录
            if (!isScrolling) {
                mLastPageIndex = pageIndex
            }
        }
        if (isScrolling && !mChangeSelectInScrolling) return
        if (pageIndex >= 0) {
            if (null != mPageListener) {
                mPageListener!!.onPageSelect(pageIndex)
            }
        }
    }

    /**
     * 设置是否在滚动状态更新选中页码
     *
     * @param changeSelectInScrolling true：更新、false：不更新
     */
    fun setChangeSelectInScrolling(changeSelectInScrolling: Boolean) {
        mChangeSelectInScrolling = changeSelectInScrolling
    }

    /**
     * 设置滚动方向
     *
     * @param orientation 滚动方向
     * @return 最终的滚动方向
     */
    @OrientationType
    fun setOrientationType(@OrientationType orientation: Int): Int {
        if (mOrientation == orientation || mScrollState != SCROLL_STATE_IDLE) return mOrientation
        mOrientation = orientation
        mItemFrames.clear()
        val x = offsetX
        val y = offsetY
        offsetX = y / usableHeight * usableWidth
        offsetY = x / usableWidth * usableHeight
        val mx = mMaxScrollX
        val my = mMaxScrollY
        mMaxScrollX = my / usableHeight * usableWidth
        mMaxScrollY = mx / usableWidth * usableHeight
        return mOrientation
    }

    //--- 滚动到指定位置 -----------------------------------------------------------------------------
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        val targetPageIndex = getPageIndexByPos(position)
        smoothScrollToPage(targetPageIndex)
    }

    /**
     * 平滑滚动到上一页
     */
    fun smoothPrePage() {
        smoothScrollToPage(pageIndexByOffset - 1)
    }

    /**
     * 平滑滚动到下一页
     */
    fun smoothNextPage() {
        smoothScrollToPage(pageIndexByOffset + 1)
    }

    /**
     * 平滑滚动到指定页面
     *
     * @param pageIndex 页面下标
     */
    fun smoothScrollToPage(pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= mLastPageCount) {
            Log.e(
                TAG,
                "pageIndex is outOfIndex, must in [0, $mLastPageCount)."
            )
            return
        }
        if (null == mRecyclerView) {
            Log.e(TAG, "RecyclerView Not Found!")
            return
        }

        // 如果滚动到页面之间距离过大，先直接滚动到目标页面到临近页面，在使用 smoothScroll 最终滚动到目标
        // 否则在滚动距离很大时，会导致滚动耗费的时间非常长
        val currentPageIndex = pageIndexByOffset
        if (Math.abs(pageIndex - currentPageIndex) > 3) {
            if (pageIndex > currentPageIndex) {
                scrollToPage(pageIndex - 3)
            } else if (pageIndex < currentPageIndex) {
                scrollToPage(pageIndex + 3)
            }
        }

        // 具体执行滚动
        val smoothScroller: LinearSmoothScroller = PagerGridSmoothScroller(mRecyclerView!!)
        val position = pageIndex * mOnePageSize
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    //=== 直接滚动 ===
    override fun scrollToPosition(position: Int) {
        val pageIndex = getPageIndexByPos(position)
        scrollToPage(pageIndex)
    }

    /**
     * 上一页
     */
    fun prePage() {
        scrollToPage(pageIndexByOffset - 1)
    }

    /**
     * 下一页
     */
    fun nextPage() {
        scrollToPage(pageIndexByOffset + 1)
    }

    /**
     * 滚动到指定页面
     *
     * @param pageIndex 页面下标
     */
    fun scrollToPage(pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= mLastPageCount) {
            Log.e(
                TAG,
                "pageIndex = $pageIndex is out of bounds, mast in [0, $mLastPageCount)"
            )
            return
        }
        if (null == mRecyclerView) {
            Log.e(TAG, "RecyclerView Not Found!")
            return
        }
        var mTargetOffsetXBy = 0
        var mTargetOffsetYBy = 0
        if (canScrollVertically()) {
            mTargetOffsetXBy = 0
            mTargetOffsetYBy = pageIndex * usableHeight - offsetY
        } else {
            mTargetOffsetXBy = pageIndex * usableWidth - offsetX
            mTargetOffsetYBy = 0
        }
//        Log.e("mTargetOffsetXBy = $mTargetOffsetXBy")
//        Log.e("mTargetOffsetYBy = $mTargetOffsetYBy")
        mRecyclerView!!.scrollBy(mTargetOffsetXBy, mTargetOffsetYBy)
        setPageIndex(pageIndex, false)
    }

    //--- 对外接口 ----------------------------------------------------------------------------------
    private var mPageListener: PageListener? = null
    fun setPageListener(pageListener: PageListener?) {
        mPageListener = pageListener
    }

    interface PageListener {
        /**
         * 页面总数量变化
         *
         * @param pageSize 页面总数
         */
        fun onPageSizeChanged(pageSize: Int)

        /**
         * 页面被选中
         *
         * @param pageIndex 选中的页面
         */
        fun onPageSelect(pageIndex: Int)
    }

    companion object {
        private val TAG = PagerGridLayoutManager::class.java.simpleName
        const val VERTICAL = 0 // 垂直滚动
        const val HORIZONTAL = 1 // 水平滚动
    }

    /**
     * 构造函数
     *
     * @param rows        行数
     * @param columns     列数
     * @param orientation 方向
     */
    init {
        mItemFrames = SparseArray<Rect>()
        mOrientation = orientation
        mRows = rows
        mColumns = columns
        mOnePageSize = mRows * mColumns
    }
}