package com.live.simple2

import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.os.MessageQueue
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.live.simple2.proformance.UiPromanceManager
import com.live.simple2.utils.DialogUtil
import com.live.simple2.view.RuningAdView
import com.live.simple2.view.TestView
import com.live.simple2.view.TextSwitchView
import com.live.simple2.view.animator.TestAnimatorActivity
import com.test.aidl.IMyPrintServerlInterface

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.toAnimator).also {

        }
    }

}

