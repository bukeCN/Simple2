package com.live.simple2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.os.MessageQueue
import android.util.Log
import android.view.View
import android.widget.TextView
import com.live.simple2.proformance.UiPromanceManager
import com.live.simple2.view.RuningAdView
import com.live.simple2.view.TextSwitchView

class MainActivity : AppCompatActivity() {

    lateinit var testView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<View>(R.id.test).setOnClickListener {
//            startActivity(Intent(this,OtherActivity::class.java))
            testView.top = testView.top - 10
        }



    }
}

