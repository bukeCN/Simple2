package com.live.simple2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.View

class MainActivity : AppCompatActivity() {

    lateinit var runingView : RuningLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        runingView = findViewById(R.id.runingView)

        findViewById<View>(R.id.start).setOnClickListener({
            runingView.update("动起来！！！！动起来！！！")
        })


    }
}