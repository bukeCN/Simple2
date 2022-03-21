package com.example.simple3.view

import android.content.res.Resources
import android.util.TypedValue

fun Int.toPx(): Int {
    return this.toFloat().toPx()
}

fun Float.toPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    ).toInt()
}
