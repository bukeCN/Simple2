package com.example.simple3.recyclerview

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by alvince on 2019/3/4
 *
 * @author y.zhang@neoclub.cn
 * @since 4.4.5
 */
open class PagingHelper(val trigger: Int = 2) {

    private var dx: Int = 0
    private var dy: Int = 0

    interface OnPagingListener {
        fun onLoadMore()
    }

    interface OnPagingItemPosListener{
        fun onRVItemPos(offest:Int)
    }


    var pagingListener: OnPagingListener? = null
    var pagingItemPosListener:OnPagingItemPosListener ?= null

    fun attach(target: RecyclerView, lifecycle: Lifecycle?) {
        attachTo(target)
        lifecycle?.also { bindToLifecycle(lifecycle) }
    }

    fun attachTo(target: RecyclerView) {
        target.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerView.takeIf {
                    newState != RecyclerView.SCROLL_STATE_DRAGGING
                }?.let {
                    it.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
                }?.takeIf {
                    val lastPos = it.findLastVisibleItemPosition()
                    recyclerView.adapter?.let { adapter ->
                        lastPos.plus(trigger) >= adapter.itemCount.minus(1)
                    } ?: false
                }?.takeIf { dx != 0 || dy != 0 }?.also {
                    pagingListener?.onLoadMore()
                }
                dx = 0
                dy = 0
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                pagingItemPosListener?.onRVItemPos(dy)
                this@PagingHelper.dx = dx
                this@PagingHelper.dy = dy
            }
        })
    }

    fun bindToLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestory() {
                onCleared()
                lifecycle.removeObserver(this)
            }

        })
    }

    open fun onCleared() {
        pagingListener = null
        pagingItemPosListener = null
    }
}

fun onPagingListener(loadMore: () -> Unit): PagingHelper.OnPagingListener =
    object : PagingHelper.OnPagingListener {
        override fun onLoadMore() {
            loadMore.invoke()
        }
    }


fun onPagingItemPosListener(block:(offest:Int)->Unit):PagingHelper.OnPagingItemPosListener =
    object :PagingHelper.OnPagingItemPosListener{
        override fun onRVItemPos(offest: Int) {
            block(offest)
        }
    }
