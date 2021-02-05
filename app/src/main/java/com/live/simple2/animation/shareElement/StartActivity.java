package com.live.simple2.animation.shareElement;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.live.simple2.R;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private ImageView startImg;
    private RecyclerView recyclerView;

    private ArrayList<String> pics = new ArrayList<>();

    private GridLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_shareelement_start);
        startImg = findViewById(R.id.startImg);
        recyclerView = findViewById(R.id.recyclerView);

        pics.add("https://cdn.sspai.com/article/365d4624-883e-ceea-f475-f6e06e0d7a88.png?imageMogr2/auto-orient/quality/95/thumbnail/!1420x708r/gravity/Center/crop/1420x708/interlace/1");
        pics.add("https://cdn.sspai.com/2021/02/04/f68ab27c9955a0ff8cf1e91069a09800.png?imageView2/2/w/1120/q/90/interlace/1/ignore-error/1");
        pics.add("https://cdn.sspai.com/2021/02/04/b49da3a37d05aa605ef9334339186ffa.png?imageView2/2/w/1120/q/90/interlace/1/ignore-error/1");
        pics.add("https://cdn.sspai.com/2021/02/03/bb8740027c884869eed801bfea970e78.jpeg?imageView2/2/w/1120/q/90/interlace/1/ignore-error/1");


        startImg.setOnClickListener(view -> {
            ViewCompat.setTransitionName(startImg, "stratImg");

            Intent intent = new Intent(this, TargetActivity.class);
            Pair<View, String> pair1 = new Pair<>(startImg, ViewCompat.getTransitionName(startImg));
            /**
             *4、生成带有共享元素的Bundle，这样系统才会知道这几个元素需要做动画
             */
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair1);
            ActivityCompat.startActivity(this, intent, activityOptionsCompat.toBundle());
        });

        layoutManager = new GridLayoutManager(this, 2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new RecyclerView.Adapter<VH>() {
            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                VH vh = new VH(getLayoutInflater().inflate(R.layout.activity_animation_shareelement_start_item, null));
                return vh;
            }

            @Override
            public void onBindViewHolder(@NonNull VH holder, int position) {
                Glide.with(holder.itemView.getContext()).load(pics.get(position)).into(holder.imageView);
                holder.imageView.setOnClickListener(view -> {

                    TargetActivity.open(StartActivity.this, pics.get(position),pics, holder.imageView,
                            recyclerView.getChildAt(layoutManager.findFirstVisibleItemPosition()), position);
//                    ViewCompat.setTransitionName(holder.imageView, "stratImg");
//                    Intent intent = new Intent(StartActivity.this, TargetActivity.class);
//                    Pair<View, String> pair1 = new Pair<>(holder.imageView, ViewCompat.getTransitionName(holder.imageView));
//                    /**
//                     *4、生成带有共享元素的Bundle，这样系统才会知道这几个元素需要做动画
//                     */
//                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(StartActivity.this, pair1);
//
//                    Bundle bundle = activityOptionsCompat.toBundle();
//                    intent.putExtra("pic",pics.get(position));
//
//                    StartActivity.this.startActivity(intent,bundle);
                });
            }

            @Override
            public int getItemCount() {
                return pics.size();
            }
        });
    }

    private static class VH extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public VH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
        }
    }
}
