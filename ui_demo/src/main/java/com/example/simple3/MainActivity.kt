package com.example.simple3

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.simple3.recyclerview.BaseAdapter
import com.example.simple3.view.*
import kotlin.concurrent.thread
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView

    lateinit var refrsh_btn: View

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn_to_behavior).setOnClickListener {
            startActivity(Intent(this, BehaviorTestActivity::class.java))
        }
        recyclerView = findViewById(R.id.recycler_view)
        refrsh_btn = findViewById(R.id.refrsh_btn)

//        val adapter =
//            object : BaseQuickAdapter<TestBean, BaseViewHolder>(R.layout.rv_item) {
//                override fun convert(holder: BaseViewHolder, item: TestBean) {
//                    holder.itemView.findViewById<TextView>(R.id.text).text= item.id
//                }
//            }
        val adapter = object : RecyclerView.Adapter<BaseViewHolder>(){
            lateinit var datas: List<TestBean>


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, null)
                return BaseViewHolder(view)
            }

            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                val item = datas[position]
                holder.itemView.findViewById<TextView>(R.id.text).text= item.id
            }

            override fun getItemCount(): Int {
                return 10
            }

        }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        val list = mutableListOf<TestBean>()

        repeat(10) {
            list.add(TestBean("100$it", it))
        }

        adapter.datas = list

        refrsh_btn.setOnClickListener {
            // 新数据
            val newList = mutableListOf<TestBean>()

            repeat(10) {
                if (it % 3 == 0) {
                    newList.add(TestBean("222$it", it))
                } else {
                    newList.add(TestBean("100$it", it))
                }
            }

            adapter.datas = newList

            val result = DiffUtil.calculateDiff(DiffCallback(newList, list))
            result.dispatchUpdatesTo(adapter)
        }
    }
}

class DiffCallback(val newDataList: List<TestBean>, val oldDataList: List<TestBean>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldDataList.size
    }

    override fun getNewListSize(): Int {
        return newDataList.size
    }

    /**
     * item 是否一致
     * @param oldItemPosition Int
     * @param newItemPosition Int
     * @return Boolean
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newData = newDataList[newItemPosition]
        val oldData = oldDataList[oldItemPosition]
        return newData.id == oldData.id && newData.index == oldData.index
    }

    /**
     * UI 视觉呈现是否发生了变化，需要 areItemsTheSame 返回 true 才会调用
     * @param oldItemPosition Int
     * @param newItemPosition Int
     * @return Boolean
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newData = newDataList[newItemPosition]
        val oldData = oldDataList[oldItemPosition]
        return newData.id != oldData.id
    }

}

class TestBean(var id: String, var index: Int)
