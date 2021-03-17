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
import android.widget.Toast
import com.live.simple2.proformance.UiPromanceManager
import com.live.simple2.view.RuningAdView
import com.live.simple2.view.TextSwitchView

class MainActivity : AppCompatActivity() {

    lateinit var testView: View
    lateinit var testView2: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        testView = findViewById<View>(R.id.test)
        testView2 = findViewById<View>(R.id.test2)

        testView.setOnClickListener {
//            testView2.offsetLeftAndRight(10)
            testView2.left = testView2.left - 10
        }

        testView2.setOnClickListener {
            Toast.makeText(this, "位置不对" + testView2.x, Toast.LENGTH_SHORT).show()
        }




    }
}

