package com.live.simple2.animation.shareElement;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;

import com.live.simple2.R;

public class StartActivity extends AppCompatActivity {
    private ImageView startImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_shareelement_start);
        startImg = findViewById(R.id.startImg);

        ViewCompat.setTransitionName(startImg,"stratImg");

        startImg.setOnClickListener( view -> {
            Intent intent = new Intent(this,TargetActivity.class);
            Pair<View,String> pair1 = new Pair<>(startImg,ViewCompat.getTransitionName(startImg));
            /**
             *4、生成带有共享元素的Bundle，这样系统才会知道这几个元素需要做动画
             */
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair1);
            ActivityCompat.startActivity(this,intent,activityOptionsCompat.toBundle());
        });

    }
}
