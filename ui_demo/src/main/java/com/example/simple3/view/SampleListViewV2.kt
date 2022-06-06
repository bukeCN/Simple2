package com.example.simple3.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
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
 * 不支持头部、中间插入！会导致事件绑定出错。
 */
open class SampleListViewV2(context: Context, attrs: AttributeSet?) :
    SwipeRefreshLayout(context, attrs) {

    /**
     * 布局资源
     */
    private val layoutResId = R.layout.com_sample_list_view

    private var tipsTv: TextView

    private var loadingView: ContentLoadingProgressBar

    private var emptyImg: ImageView

    private var recyclerView: RecyclerView

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
        init: LaborerV2<T>.() -> Unit
    ): LaborerV2<T> {
        return LaborerV2<T>(recyclerView, this, lifecycle, itemRes).apply(init).apply {

            // 可操作区域点击了，
            actionLayout.setOnClickListener {
                this.actionAreaClick()
            }

            showEptyViewFun = { showEmptyView() }
            showContentViewFun = { showContentView() }
            showLoadingViewFun = { showLoadingView() }
            showErrorViewFun = {
                showErrorView(it)
            }
        }
    }


    /**
     * 👷🏻‍♂来一个打工人，负责干活。针对静态列表 View
     */
    fun <T> hireLaborerForStatic(
        lifecycle: Lifecycle,
        @LayoutRes itemRes: Int,
        init: LaborerV2<T>.() -> Unit
    ): LaborerV2<T> {
        return LaborerV2<T>(recyclerView, this, lifecycle, itemRes).apply(init).apply {
            isPadding = false
            isEnabledRefresh = false
            isEnableEmptyView = false

            // 可操作区域点击了，
//            actionLayout.setOnClickListener {
//                this.actionAreaClick()
//            }
//
//            showEptyViewFun = { showEmptyView() }
//            showContentViewFun = { showContentView() }
//            showLoadingViewFun = { showLoadingView() }
//            showErrorViewFun = {
//                showErrorView(it)
//            }
        }
    }


}

/**
 * 负责处理 RecyclerView 的事件
 * 目标是使用 DSL 语法风格调用，实现列表数据
 */
public class LaborerV2<T>(
    private val hostRecyclerView: RecyclerView,
    private val swipeRefreshLayout: SwipeRefreshLayout,
    private val lifecycle: Lifecycle,
    @LayoutRes private val itemRes: Int
) : RecyclerView.AdapterDataObserver() {

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
     * 在作为简单列表无需接口调用的功能可以使用，默认关闭
     */
    var isEnableEmptyView = false

    /**
     * 是否开启下拉刷新，默认开启
     */
    var isEnabledRefresh: Boolean = true
        set(value) {
            // 在 LaborerV2 初始化的时候设置过了一次，后续修改需要再次设置
            value.also { swipeRefreshLayout.isEnabled = it }
            field = value
        }

    /**
     * 可自定义
     */
    var mOrientation: Int = RecyclerView.VERTICAL

    init {
        // 默认关闭下拉刷新
        swipeRefreshLayout.isEnabled = isEnabledRefresh

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    baseAdapter?.let {
                        if (it.hasObservers()) {
                            baseAdapter.unregisterAdapterDataObserver(this@LaborerV2)
                        }
                    }
                }
            }
        })
    }

    internal var showEptyViewFun: (() -> Unit)? = null

    internal var showContentViewFun: (() -> Unit)? = null

    internal var showLoadingViewFun: (() -> Unit)? = null

    internal var showErrorViewFun: ((msg: String) -> Unit)? = null

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
    private val baseAdapter = object : BaseQuickAdapter<T, BaseViewHolder>(itemRes) {
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
                if (isEnableEmptyView) {
                    showEptyViewFun?.invoke()
                }
            }
        }
    }


    internal fun actionAreaClick() {
        if (isInError) {
            showLoadingViewFun?.invoke()

            onTryLoad?.invoke {// 调用函数
                if (it.isEmpty()) {
                    // 显示空页面
                    if (isEnableEmptyView) {
                        showEptyViewFun?.invoke()
                    }
                } else {
                    showContentViewFun?.invoke()
                    baseAdapter.setNewInstance(it)
                }
            }
        }
        isInError = false
    }

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
                        baseAdapter.setNewInstance(it)
                    }
                }
            }

            // 监控数据改变，为空时及时显示空页面
            baseAdapter?.apply {
                registerAdapterDataObserver(this@LaborerV2)
            }

            hostRecyclerView.apply {
                layoutManager = LinearLayoutManager(hostRecyclerView.context, mOrientation, false)

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
     * 显示错误提示，调用此函数标识可以触发点击重试
     */
    fun setErrorMessage(msg: String) {
        swipeRefreshLayout.isRefreshing = false

        isInError = true
        showErrorViewFun?.invoke(msg)
    }

    /**
     * 开始获取数据，干活！
     */
    fun letsGoLabrer(newData: MutableList<T>? = null) {
        newData?.apply {
            baseAdapter.setNewInstance(newData)
        } ?: kotlin.run {
            if (isEnableEmptyView) {
                showLoadingViewFun?.invoke()
            } else {
                showContentViewFun?.invoke()
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
                    baseAdapter.setNewInstance(it)
                }
            }
        }
    }

    /**
     * 清除数据，重新获取。
     * 不会走初始化流程，然后走重新获取数据流程
     */
    fun againLetsGoLabrer() {
        // 清除数据
        baseAdapter.setNewData(mutableListOf())

        // 重新拉取数据
        letsGoLabrer()
    }

    /**
     * 直接设置数据
     * @param newData MutableList<T>
     */
    fun setNewData(newData: MutableList<T>){
        letsGoLabrer(newData)
    }

    /**
     * 添加更多数据
     * @param newData MutableList<T>
     */
    fun addMoreData(newData: MutableList<T>){
        baseAdapter.addData(newData)
    }

    /**
     * 删除单个 item
     * @param position Int
     */
    fun removeItem(position: Int) {
        baseAdapter?.remove(position)
    }
}
