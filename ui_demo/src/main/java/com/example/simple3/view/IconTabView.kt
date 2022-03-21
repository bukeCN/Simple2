package com.example.simple3.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.simple3.R

class IconTabView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    private val ITEM_RES = R.layout.com_icon_tab_view_item

    var onTabSelectChangeListener: ((position: Int) -> Unit)? = null

    var currentItemSelect = 0
        set(value) {
            field = value
            iconTabAdapter.updateSelectItem(field)
        }

    var tabList = arrayListOf<IconTabItem>()
        set(value) {
            field = value
            iconTabAdapter.setNewData(field)
        }

    private val iconTabAdapter: IconTabAdapter by lazy {
        IconTabAdapter(ITEM_RES)
    }

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        iconTabAdapter.apply {
            adapter = this

            setOnItemClickListener { adapter, view, position ->
                currentItemSelect = position
                updateSelectItem(currentItemSelect)
                onTabSelectChangeListener?.invoke(currentItemSelect)
            }
        }
    }
}

class IconTabAdapter(itemRes: Int) : BaseQuickAdapter<IconTabItem, BaseViewHolder>(itemRes) {
    private var currentSelectItem  = 0

    fun updateSelectItem(position: Int) {
        if (currentSelectItem == position) return
        currentSelectItem = position
        notifyDataSetChanged()
    }

    override fun convert(helper: BaseViewHolder, item: IconTabItem) {
        val position = helper.adapterPosition

        // icon 设置
        val iconImg = helper.getView<ImageView>(R.id.img_icon)
        if (item.provideIconForUrl().isNotEmpty()){
//            UkiImgLoader.justLoad(iconImg.context, item.provideIconForUrl(), iconImg)
        } else if(item.provideIconForResouse() != 0){
            iconImg.setImageResource(item.provideIconForResouse())
        }
        // 文字
        helper.getView<TextView>(R.id.tv_icon).text = item.provideIconText()

        // 临时指示器
        helper.getView<View>(R.id.indicator).visibility = if(currentSelectItem == position) View.VISIBLE else View.INVISIBLE
    }
}

/**
 * Item 数据接口
 */
interface IconTabItem {

    /**
     * 提供 icon  本地 res 资源
     * @return Int
     */
    fun provideIconForResouse(): Int

    /**
     * 提供 icon 在线 url 资源，优先使用，没有再取 res
     * @return String
     */
    fun provideIconForUrl(): String

    /**
     * 提供 item 文字说明
     * @return String
     */
    fun provideIconText(): String

}
