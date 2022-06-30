package com.example.simple3.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.simple3.R

class MyFragmentActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MyFragmentActivity"
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e(TAG, "onRestart")
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate")
        setContentView(R.layout.my_fragment_activity)

        val oneFragment = OneFragment()
        val twoFragment = TwoFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.content_layout, oneFragment)
        transaction.setMaxLifecycle(oneFragment, Lifecycle.State.CREATED)
        transaction.addToBackStack(null)
        transaction.commit()

        findViewById<View>(R.id.btn_action).setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setMaxLifecycle(oneFragment, Lifecycle.State.CREATED)
            transaction.replace(R.id.content_layout, twoFragment)
            transaction.addToBackStack(null)
            transaction.commitNow()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.e(TAG, "onSaveInstanceState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e(TAG, "onRestoreInstanceState")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.e(TAG, "onDetachedFromWindow")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
    }

}