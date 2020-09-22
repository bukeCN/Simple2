package com.live.customview

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.live.customview.chuizi_switch.ChuiziSwitchView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var c = ChuiziSwitchView(this)

        var msg = Message()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            msg.isAsynchronous = true
        }
        msg.target = null

    }
}
