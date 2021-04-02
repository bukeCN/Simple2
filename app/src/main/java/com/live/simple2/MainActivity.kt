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
import android.widget.TextView
import android.widget.Toast
import com.live.simple2.proformance.UiPromanceManager
import com.live.simple2.view.RuningAdView
import com.live.simple2.view.TextSwitchView

class MainActivity : AppCompatActivity() {

    lateinit var testView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testView = findViewById<View>(R.id.test)
        testView.setOnClickListener {
            Toast.makeText(this, "哈哈",  Toast.LENGTH_SHORT).show()
        }
        testView.post {
            Log.e("sun", "执行 post()")
        }
    }
}

