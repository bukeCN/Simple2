package com.live.simple2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.os.MessageQueue
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.live.simple2.proformance.UiPromanceManager
import com.live.simple2.utils.DialogUtil
import com.live.simple2.view.RuningAdView
import com.live.simple2.view.TextSwitchView

class MainActivity : AppCompatActivity() {

    lateinit var testView: Button
    lateinit var tGroup: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testView = findViewById<Button>(R.id.test)
        tGroup = findViewById<View>(R.id.tGroup)
        testView.setOnClickListener {
            testView.text = "你好呀"
        }
    }
}

