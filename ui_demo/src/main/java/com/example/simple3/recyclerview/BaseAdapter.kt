package com.example.simple3.recyclerview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simple3.R
import kotlin.random.Random

class BaseAdapter(var dataList: List<String>) : RecyclerView.Adapter<BaseAdapter.ViewHolder>() {

    class ViewHolder(var item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.com_sample_list_view_item, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.findViewById<TextView>(R.id.action).text = dataList.get(position)
        holder.item.setBackgroundColor(color())
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun color(): Int {
        val random = java.util.Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)
        return Color.rgb(r, g, b)
    }
}