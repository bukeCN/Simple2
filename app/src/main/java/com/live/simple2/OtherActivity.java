package com.live.simple2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.live.simple2.animation.shareElement.StartActivity;

public class OtherActivity extends AppCompatActivity {
    Button button;

    StringBuilder str = new StringBuilder();
    boolean pause = false;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.e("sun","处理消息");
                button.requestLayout();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity);
        button = findViewById(R.id.test);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setFloatValues(0, 100);
        valueAnimator.setDuration(6000*10);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.e("sun", "动画**" + animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
//        valueAnimator.start();
//        valueAnimator.cancel();
        button.setOnClickListener( view -> {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(1);
//                    Looper.prepare();
//                    Handler handler = new Handler(Looper.myLooper()){
//                        @Override
//                        public void handleMessage(@NonNull Message msg) {
//                            super.handleMessage(msg);
////                            if (!pause){
////                                valueAnimator.pause();
////                                pause = true;
////                            } else {
////                                valueAnimator.resume();
////                            }
//                        }
//                    };
//                    handler.sendEmptyMessageDelayed(0, 3000);
//                    Looper.loop();
                }
            });
            thread.start();
        });
    }

    public static void printStackTrace(){
        StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                System.out.println(stackElements[i].getFileName() + "." + stackElements[i].getMethodName() + "()");
            }
            System.out.println("-----------------------------------");
        }
    }

}
