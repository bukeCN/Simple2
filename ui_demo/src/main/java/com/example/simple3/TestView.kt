package com.example.simple3

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class TestView(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
        init {
            println("haha")
        }
}