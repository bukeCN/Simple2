package com.example.simple3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simple3.recyclerview.BaseAdapter

class BehaviorTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.behavior_layout)

        val list = mutableListOf<String>()
        repeat(100){
            list.add(it.toString())
        }

        findViewById<RecyclerView>(R.id.recycler_view)?.apply {
            layoutManager = LinearLayoutManager(this@BehaviorTestActivity,
                RecyclerView.VERTICAL,false)
            adapter = BaseAdapter(list)
        }
    }
}