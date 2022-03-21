package com.example.simple3.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.updateLayoutParams
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.simple3.R
import com.example.simple3.recyclerview.PagingHelper

/**
 * 简单列表显示页面，包含：
 * 1. 空白页面、错误(可重试)、内容页
 * 2. 分页加载
 * 3. 下拉刷新
 * 最佳实践@look[PartyBlackListActivity]
 *  ag.
SampleListView.hireLabrer<String>(lifecycle,R.layout.com_sample_list_view_item){

// 开启下拉刷新
isEnabledRefresh(true)

// 填充适配器
itemConvert = { holder, item ->
holder.getView<TextView>(R.id.action).text = item
// 添加点击事件
addClickLstener(holder.getView<TextView>(R.id.action), holder.adapterPosition)
}

// 点击事件集中处理
listItemOnClick = { id: Int, position: Int ->
when(id){
R.id.action -> {
Toast.makeText(this@RecyclerViewActivity,"点击了${position}",Toast.LENGTH_SHORT).show()
}
}
}

// 首次请求数据触发
onFristLoad = { setDataAction ->
sampleListView.postDelayed({ setError("出错了，请重试哦！")},3000)
}

// 下拉刷新数据触发
onRefreshLoad = { setDataAction ->
setError("出错了，请重试哦！")
}

// 加载更多触发
onPageLoadMore = { loadPageIndex, addDataAction ->
val list = arrayListOf("-1","-2","-3","-4","-5","-6","-7","-8")
// 添加数据
addDataAction(list)
}

// 触发重试
onTryLoad = { setDataAction ->
sampleListView.postDelayed({
val list = arrayListOf("1","2","3","4","5","6","7","8","1","2","3","4","5","6","7","8")
// 添加数据
setDataAction(list)
},3000)
}

}.also {
it.letsGoLabrer()
}
 */
open class SampleListView(context: Context, attrs: AttributeSet?) :
    SwipeRefreshLayout(context, attrs) {

    /**
     * 布局资源
     */
    private val layoutResId = R.layout.com_sample_list_view

    private var tipsTv: TextView

    private var loadingView: ContentLoadingProgressBar

    private var emptyImg: ImageView

    private var recyclerView: RecyclerView

    private var wrap_view: View

    val static_sample_list = true // 静态列表，高度内容自适应

    /**
     * 状态提示区域
     * 添加点击处理，至于是那种状态交给打工人处理，这里不负责。
     */
    private var actionLayout: View

    constructor(context: Context) : this(context, null)

    init {
        inflate(context, layoutResId, this)

        tipsTv = findViewById(R.id.tips_tv)
        recyclerView = findViewById(R.id.recycler_view)
        loadingView = findViewById(R.id.loading_progress)
        emptyImg = findViewById(R.id.empty_img)
        actionLayout = findViewById(R.id.action_layout)
        wrap_view = findViewById(R.id.wrap_view)

//        val typedArray = context.obtainStyledAttributes()
    }


    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return super.generateLayoutParams(p).apply {
            height = LayoutParams.WRAP_CONTENT
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return super.generateLayoutParams(attrs).apply {
            height = LayoutParams.WRAP_CONTENT
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (static_sample_list) {
            recyclerView.updateLayoutParams {
                height = LayoutParams.WRAP_CONTENT
            }

            wrap_view.updateLayoutParams {
                height = LayoutParams.WRAP_CONTENT
            }
        }
    }

    private fun showEmptyView() {
        loadingView.hide()
        actionLayout.visibility = View.VISIBLE
        emptyImg.visibility = View.VISIBLE
        tipsTv.text = "此处空空如也"

        recyclerView.visibility = View.GONE
    }

    private fun showErrorView(msg: String = "出错了，点击重试！") {
        loadingView.hide()
        actionLayout.visibility = View.VISIBLE
        emptyImg.visibility = View.VISIBLE
        tipsTv.text = msg

        recyclerView.visibility = View.GONE
    }

    private fun showLoadingView() {
        loadingView.show()
        actionLayout.visibility = View.VISIBLE
        emptyImg.visibility = View.GONE
        tipsTv.text = "内容加载中..."

        recyclerView.visibility = View.GONE
    }

    private fun showContentView() {
        loadingView.hide()
        actionLayout.visibility = View.GONE

        recyclerView.visibility = View.VISIBLE
    }

    /**********
     * 对外函数
     ********/

    /**
     * 👷🏻‍♂来一个打工人，负责干活。
     */
    fun <T> hireLaborer(
        lifecycle: Lifecycle,
        @LayoutRes itemRes: Int,
        init: Laborer<T>.() -> Unit
    ): Laborer<T> {
        return Laborer<T>(recyclerView, this, lifecycle, itemRes).apply(init).apply {

            // 可操作区域点击了，
            actionLayout.setOnClickListener {
                actionAreaClick()
            }

            showEptyViewFun = { showEmptyView() }
            showContentViewFun = { showContentView() }
            showLoadingViewFun = { showLoadingView() }
            showErrorViewFun = {
                showErrorView(it)
            }
        }
    }
}

/**
 * 负责处理 RecyclerView 的事件
 * 目标是使用 DSL 语法风格调用，实现列表数据
 */
public class Laborer<T>(
    private val recyclerView: RecyclerView,
    private val swipeRefreshLayout: SwipeRefreshLayout,
    private val lifecycle: Lifecycle,
    @LayoutRes private val itemRes: Int
) : RecyclerView.AdapterDataObserver() {

    val host = recyclerView

    /**
     * 分页 index
     */
    private var pageIndex = 0

    /**
     * 是否处于 error 状态
     */
    private var isInError = false

    /**
     * 是否已经初始化过
     */
    private var isInit = false

    /**
     * 是否开启分页，默认开启
     */
    var isPadding = true

    /**
     * 是否开启空页面、加载中、空布局显示。
     * 在作为简单列表无需接口调用的功能可以使用
     */
    var isEnableEmptyView = true

    var mLayoutManager: RecyclerView.LayoutManager =
        LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)

    init {
        isEnabledRefresh(false)

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    baseAdapter?.let {
                        if (it.hasObservers()) {
                            baseAdapter.unregisterAdapterDataObserver(this@Laborer)
                        }
                    }
                }
            }
        })
    }

    var showEptyViewFun: (() -> Unit)? = null

    var showContentViewFun: (() -> Unit)? = null

    var showLoadingViewFun: (() -> Unit)? = null

    var showErrorViewFun: ((msg: String) -> Unit)? = null

    /**
     * Item 以及 Item 内部点击事件
     */
    var itemOnClick: ((id: Int, position: Int, item: T) -> Unit)? = null

    /**
     * 首次加载数据调用
     * action 是设置数据调用
     */
    var onFristLoad: ((page: Int, setDataAction: (datas: MutableList<T>) -> Unit) -> Unit)? = null

    /**
     * 刷新数据调用
     * action 是设置数据调用
     */
    var onRefreshLoad: ((setDataAction: (datas: MutableList<T>) -> Unit) -> Unit)? = null

    /**
     * 加载更多调用
     * action 是设置数据调用
     */
    var onPageLoadMore: ((loadPageIndex: Int, lastItem: T, addDataAction: (datas: MutableList<T>) -> Unit) -> Unit)? =
        null

    /**
     * 加载出错，重试调用。相当于刷新。
     * action 是设置数据调用
     */
    var onTryLoad: ((setDataAction: (datas: MutableList<T>) -> Unit) -> Unit)? = null

    /**
     * 适配器
     */
    val baseAdapter = object : BaseQuickAdapter<T, BaseViewHolder>(itemRes) {
        override fun convert(holder: BaseViewHolder, item: T) {
            itemConvert?.invoke(holder, item)
        }
    }

    /**
     * 负责适配器填充
     */
    var itemConvert: ((holder: BaseViewHolder, item: T) -> Unit)? = null

    override fun onChanged() {
        super.onChanged()
        baseAdapter?.apply {
            if (itemCount == 0) {
                showEptyViewFun?.invoke()
            }
        }
    }

    /**
     * 显示错误提示，调用此函数标识可以点击重试
     */
    fun setError(msg: String) {
        swipeRefreshLayout.isRefreshing = false

        isInError = true
        showErrorViewFun?.invoke(msg)
    }

    fun actionAreaClick() {
        if (isInError) {
            showLoadingViewFun?.invoke()

            onTryLoad?.invoke {// 调用函数
                if (it.isEmpty()) {
                    // 显示空页面
                    showEptyViewFun?.invoke()
                } else {
                    showContentViewFun?.invoke()
                    baseAdapter.setNewData(it)
                }
            }
        }
        isInError = false
    }

    /**
     * 是否开启下拉刷新默认 false
     */
    fun isEnabledRefresh(enabled: Boolean) = enabled.also { swipeRefreshLayout.isEnabled = it }


    fun addClickLstener(view: View, position: Int) {
        view.setOnClickListener {
            baseAdapter?.apply {
                getItem(position)?.let { it1 -> itemOnClick?.invoke(view.id, position, it1) }
            }
        }
    }

    fun addClickLstenerWithSpring(view: View, position: Int) {
//        view.onClickWithSpring {
//            baseAdapter?.apply {
//                getItem(position)?.let { item -> itemOnClick?.invoke(it.id, position, item) }
//            }
//        }
    }

    fun removeItem(position: Int) {
        baseAdapter?.remove(position)
    }

    private fun bossInit() {
        if (!isInit) {
            isInit = true

            // 下拉刷新
            swipeRefreshLayout.setOnRefreshListener {
                isInError = false

                onRefreshLoad?.invoke {// 调用函数
                    swipeRefreshLayout.isRefreshing = false
                    if (it.isEmpty()) {
                        // 显示空页面
                        if (isEnableEmptyView) {
                            showEptyViewFun?.invoke()
                        }
                    } else {
                        showContentViewFun?.invoke()
                        baseAdapter.setNewData(it)
                    }
                }
            }

            // 监控数据改变，为空时及时显示空页面
            baseAdapter?.apply {
                registerAdapterDataObserver(this@Laborer)
            }

            recyclerView.apply {
                layoutManager = mLayoutManager

                adapter = baseAdapter
            }.takeIf {
                isPadding  // 如果启用分页上啦加载
            }?.apply {
                val helper = PagingHelper()
                helper.attach(this, lifecycle)
                helper.pagingListener = object : PagingHelper.OnPagingListener {
                    override fun onLoadMore() {
                        pageIndex++
                        baseAdapter?.run {
                            getItem(itemCount - 1)
                        }?.let {
                            onPageLoadMore?.invoke(pageIndex, it) {// 调用函数
                                baseAdapter.addData(it)
                                if (it.isEmpty()) {// 没有加载更多，还原处理
                                    pageIndex--
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 开始获取数据，干活！
     */
    fun letsGoLabrer() {
        if (isEnableEmptyView) {
            showLoadingViewFun?.invoke()
        }

        // 初始化
        bossInit()

        // 首次加载数据
        onFristLoad?.invoke(pageIndex) {
            if (it.isEmpty()) {
                // 显示空页面
                if (isEnableEmptyView) {
                    showEptyViewFun?.invoke()
                }
            } else {
                showContentViewFun?.invoke()
                baseAdapter.setNewData(it)
            }
        }
    }

    /**
     * 清除数据，重新获取。
     * 不会走初始化流程，然后走重新获取数据流程
     */
    fun resetLetsGoLabrer() {
        // 清除数据
        baseAdapter.setNewData(mutableListOf())

        // 重新拉取数据
        letsGoLabrer()
    }
}
