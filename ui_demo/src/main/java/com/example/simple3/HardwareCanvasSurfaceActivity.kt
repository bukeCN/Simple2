package com.example.simple3

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity

class HardwareCanvasSurfaceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    class RenderingThread : Thread() {
        private lateinit var mSurface: SurfaceHolder

        @SuppressLint("NewApi")
        override fun run() {
            while (true){
                var canvas = mSurface.lockHardwareCanvas()
            }
        }
    }
}