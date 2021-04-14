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
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.live.simple2.animation.shareElement.StartActivity;

public class OtherActivity extends AppCompatActivity {
    Button button;

    StringBuilder str = new StringBuilder();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity);
        button = findViewById(R.id.test);


        button.setOnClickListener( view -> {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(OtherActivity.this, StartActivity.class));
                }
            });
            thread.start();
        });


        View view = new View(this);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setFloatValues(10, 100);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

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
        valueAnimator.start();

    }
}
