package com.example.simple3.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.marginLeft
import com.example.simple3.Utils

class LineTabsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
    View.OnClickListener, Runnable {

    private var lastSelectItemIndex = 0

    private val ITEM_MID = 20

    private var selectColor = Color.parseColor("#ff0000")
    private var normalColor = Color.parseColor("#eeeeee")

    var onTabSelectedFun: ((Int) -> Unit)? = null
    var onTabSelectChangeScrll: ((Int,Int) ->Unit)? = null // item 改变之后需要滑动的距离

    fun bindTabs(tabs: List<String>) {
        if (childCount != 0) {
            removeAllViews()
        }

        tabs.forEachIndexed { index, title ->
            val itemView = buildTextItemView(title).apply {
                if (lastSelectItemIndex == index){
                    setTextColor(selectColor)
                } else {
                    setTextColor(normalColor)
                }
            }
            addView(itemView)
        }

        post(this)
    }

    override fun run() {
        if (childCount > 0){
            val fristWidth = getChildAt(0).width
            val padding = (parent as View).width/2 - fristWidth/2
            setPadding(padding + paddingLeft, paddingTop, padding + paddingRight, paddingBottom)
        }
    }

    fun setSelectTab(index: Int){
        onClick(getChildAt(index))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(this)
    }

    private fun buildTextItemView(tabTitle: String): TextView {
        return TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            textSize = 16f
            text = tabTitle
            setOnClickListener(this@LineTabsView)
        }
    }

    override fun onClick(v: View) {
        val itemIndex = children.indexOf(v)
        if (itemIndex == lastSelectItemIndex) return

        changeSelectUi(itemIndex)
        onTabSelectedFun?.invoke(itemIndex)

        lastSelectItemIndex = itemIndex
    }

    private fun changeSelectUi(itemIndex: Int) {
        val lastItem = getChildAt(lastSelectItemIndex) as TextView
        val currentItem = getChildAt(itemIndex) as TextView

        lastItem.setTextColor(normalColor)
        currentItem.setTextColor(selectColor)

        onTabSelectChangeScrll?.invoke(currentItem.width, currentItem.left)
    }

}