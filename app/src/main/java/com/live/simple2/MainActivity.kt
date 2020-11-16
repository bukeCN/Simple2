package com.live.simple2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import android.util.Log
import android.view.View
import com.live.simple2.proformance.UiPromanceManager
import com.live.simple2.view.RuningLayout
import com.live.simple2.view.TextSwitchView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Looper.getMainLooper().queue.addIdleHandler(MyIdleHander())

        findViewById<View>(R.id.testBtn).setOnClickListener {
            it.post(Runnable {
                Log.e("sun","消息执行")
            })
        }
    }

    class MyIdleHander : MessageQueue.IdleHandler{
        var count = 0
        override fun queueIdle(): Boolean {
            Log.e("sun","空闲输出" + count)
            count++
            return true
        }

    }
}

