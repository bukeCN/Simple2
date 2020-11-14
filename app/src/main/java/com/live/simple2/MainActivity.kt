package com.live.simple2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.live.simple2.proformance.UiPromanceManager
import com.live.simple2.view.RuningLayout
import com.live.simple2.view.TextSwitchView

class MainActivity : AppCompatActivity() {

    lateinit var runingView : RuningLayout
    lateinit var sw : TextSwitchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sw = findViewById(R.id.sw)
        sw.setChecked(true)

        findViewById<View>(R.id.testBtn).setOnClickListener {
            sw.setChecked(true)
        }

        var  uiPromanceManager = UiPromanceManager()

        uiPromanceManager.monitorFPS()

        mainLooper.setMessageLogging {
            
        }

    }
}