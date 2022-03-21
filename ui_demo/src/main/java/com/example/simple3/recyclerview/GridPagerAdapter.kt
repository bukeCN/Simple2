package com.example.simple3.recyclerview

import android.content.Context
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.GridLayoutManager

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.example.simple3.R


class GridPagerAdapter(@Nullable context: Context, orientation: Int, colum: Int, row: Int) :
    RecyclerView.Adapter<GridPagerAdapter.ViewHolder>() {
    private val row: Int
    private val colum: Int
    private val orientation: Int
    private val mContext: Context
    override fun onViewRecycled(@NonNull holder: ViewHolder) {
        super.onViewRecycled(holder)
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.com_sample_list_view_item, viewGroup, false)
        val layoutParams: ViewGroup.LayoutParams = view.getLayoutParams()
        if (GridLayoutManager.VERTICAL == orientation) {
            layoutParams.height = viewGroup.height / row
        } else {
            layoutParams.width = viewGroup.width / colum
        }
        view.setLayoutParams(layoutParams)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull viewHolder: ViewHolder, i: Int) {
        viewHolder.tv_content.text = i.toString()
    }

    override fun getItemCount(): Int {
        return 50
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_content: TextView

        init {
            tv_content = itemView.findViewById(R.id.action)
        }
    }

    init {
        mContext = context
        this.orientation = orientation
        this.colum = colum
        this.row = row
    }
}