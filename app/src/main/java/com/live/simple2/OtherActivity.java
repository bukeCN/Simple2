package com.live.simple2;

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

    }
}
