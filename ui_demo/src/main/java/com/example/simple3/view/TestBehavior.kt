package com.example.simple3.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

class TestBehavior : AppBarLayout.Behavior {

    private val TAG = "TestBehavior"

    constructor() : super()

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        dependency: View
    ): Boolean {
        Log.e(TAG, "layoutDependsOn:$dependency")
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        dependency: View
    ): Boolean {
        return super.onDependentViewChanged(parent, child, dependency)
    }
}