package com.example.simple3.recyclerview

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.simple3.R
import com.example.simple3.recyclerview.mpager.MPagerLayoutManager
import com.example.simple3.recyclerview.mpager.MPagerSnapHelper
import com.example.simple3.recyclerview.page.PagerGridLayoutManager
import com.example.simple3.recyclerview.page.PagerGridSnapHelper
import com.example.simple3.recyclerview.upager.UPagerGridLayoutManager
import com.example.simple3.view.SampleListView

class RecyclerViewActivity : AppCompatActivity() {
    private lateinit var sampleListView: SampleListView

    private var dataList = arrayListOf<String>()

    companion object {
        var FLAG = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_view_activity)

        sampleListView = findViewById(R.id.sampleListView)

        val btn = findViewById<Button>(R.id.btn)

        val datas = arrayListOf<String>()

        datas.add("1")
        datas.add("2")
        datas.add("3")
        datas.add("3")

        sampleListView.hireLaborer<String>(
            lifecycle,
            R.layout.com_sample_list_view_item
        ) {

            isEnabledRefresh(false)

            isPadding = false

            // 填充适配器
            itemConvert = { holder, item ->
                holder.getView<TextView>(R.id.action).text = item

                // 添加点击事件
                addClickLstener(
                    holder.getView<TextView>(R.id.action),
                    holder.adapterPosition
                )
            }

            // 点击事件集中处理
            itemOnClick = { id, position, item ->
                when (id) {
                    R.id.action -> {
                        Toast.makeText(
                            this@RecyclerViewActivity,
                            "点击了${position}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            // 首次请求数据触发
            onFristLoad = { _, setDataAction ->
                // 添加数据
                setDataAction(datas)
            }

            // 下拉刷新数据触发
            onRefreshLoad = { _ ->
                setError("出错了，请重试哦！")
            }

            // 加载更多触发
            onPageLoadMore = { _, _, addDataAction ->
                val list = arrayListOf("-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8")
                // 添加数据
                addDataAction(list)
            }
        }.letsGoLabrer()
    }
}