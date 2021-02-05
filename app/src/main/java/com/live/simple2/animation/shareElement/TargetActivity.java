package com.live.simple2.animation.shareElement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.live.simple2.R;
import com.live.simple2.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TargetActivity extends AppCompatActivity {
    private ImageView img;

    private ViewPager2 viewPager;

    private List<String> pics;

    private String pic;
    private int selectPosition = 0;

    private static final String SHARE_NAME = "share_name";
    private static final String SHARE_PIC_KEY = "share_pic_key";
    private static final String SHARE_PIC_KEY_FOR_LIST = "share_pic_list_key";
    private static final String SHARE_PIC_KEY_FOR_SELECT= "share_pic_select_key";

    public static void open(Activity src, String currentPic, ArrayList<String> pics, ImageView srcImg){
        ViewCompat.setTransitionName(srcImg, SHARE_NAME);
        Intent intent = new Intent(src, TargetActivity.class);
        Pair<View, String> pair1 = new Pair<>(srcImg, ViewCompat.getTransitionName(srcImg));
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(src, pair1);

        Bundle bundle = activityOptionsCompat.toBundle();
        intent.putExtra(SHARE_PIC_KEY,currentPic);
        intent.putStringArrayListExtra(SHARE_PIC_KEY_FOR_LIST,pics);
        src.startActivity(intent,bundle);
    }
    public static void open(Activity src, String currentPic, ArrayList<String> pics, ImageView srcImg, View fristView, int postition){
        ViewCompat.setTransitionName(srcImg, SHARE_NAME);
        ViewCompat.setTransitionName(fristView, SHARE_NAME+1);
        Intent intent = new Intent(src, TargetActivity.class);
        Pair<View, String> pair1 = new Pair<>(srcImg, ViewCompat.getTransitionName(srcImg));
        Pair<View, String> pair2 = new Pair<>(fristView, ViewCompat.getTransitionName(fristView));
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(src, pair1, pair2);

        Bundle bundle = activityOptionsCompat.toBundle();
        intent.putExtra(SHARE_PIC_KEY, currentPic);
        intent.putExtra(SHARE_PIC_KEY_FOR_SELECT, postition);
        intent.putStringArrayListExtra(SHARE_PIC_KEY_FOR_LIST, pics);
        src.startActivity(intent,bundle);
    }
//    public static void open(Activity src, String currentPic, List<View> currentVisiableViews, ArrayList<String> pics, ImageView srcImg){
//        for (View view : currentVisiableViews){
//            ViewCompat.setTransitionName(srcImg, SHARE_NAME);
//        }
//
//        Intent intent = new Intent(src, TargetActivity.class);
//        Pair<View, String> pair1 = new Pair<>(srcImg, ViewCompat.getTransitionName(srcImg));
//        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(src, pair1);
//
//        Bundle bundle = activityOptionsCompat.toBundle();
//        intent.putExtra(SHARE_PIC_KEY,currentPic);
//        intent.putStringArrayListExtra(SHARE_PIC_KEY_FOR_LIST,pics);
//        src.startActivity(intent,bundle);
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
                Log.e("sun","onSharedElementStart");
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                Log.e("sun","onSharedElementEnd");
            }

            @Override
            public void onRejectSharedElements(List<View> rejectedSharedElements) {
                super.onRejectSharedElements(rejectedSharedElements);
                Log.e("sun","onRejectSharedElements");
            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                Log.e("sun","onMapSharedElements");
            }

            @Override
            public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
                Log.e("sun","onCaptureSharedElementSnapshot");
                return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
            }

            @Override
            public View onCreateSnapshotView(Context context, Parcelable snapshot) {
                Log.e("sun","onCreateSnapshotView");
                return super.onCreateSnapshotView(context, snapshot);
            }

            @Override
            public void onSharedElementsArrived(List<String> sharedElementNames, List<View> sharedElements, OnSharedElementsReadyListener listener) {
                Log.e("sun","onSharedElementsArrived");
                super.onSharedElementsArrived(sharedElementNames, sharedElements, listener);
            }
        });
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
                Log.e("sun","onSharedElementStart");
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                Log.e("sun","onSharedElementEnd");
            }

            @Override
            public void onRejectSharedElements(List<View> rejectedSharedElements) {
                super.onRejectSharedElements(rejectedSharedElements);
                Log.e("sun","onRejectSharedElements");
            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                Log.e("sun","onMapSharedElements");
            }

            @Override
            public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {

                Log.e("sun","onCaptureSharedElementSnapshot");
                return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
            }

            @Override
            public View onCreateSnapshotView(Context context, Parcelable snapshot) {
                Log.e("sun","onCreateSnapshotView");
                return super.onCreateSnapshotView(context, snapshot);
            }

            @Override
            public void onSharedElementsArrived(List<String> sharedElementNames, List<View> sharedElements, OnSharedElementsReadyListener listener) {
                super.onSharedElementsArrived(sharedElementNames, sharedElements, listener);
                Log.e("sun","onSharedElementsArrived");
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_shareelement_target);
        img = findViewById(R.id.targetImg);
        viewPager = findViewById(R.id.viewPager);

        pic = getIntent().getStringExtra(SHARE_PIC_KEY);

        pics = getIntent().getStringArrayListExtra(SHARE_PIC_KEY_FOR_LIST);

        selectPosition = getIntent().getIntExtra(SHARE_PIC_KEY_FOR_SELECT, 0);

        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        if (!StringUtil.isEmpty(pic)){
            Glide.with(this).load(pic).into(img);

            ViewCompat.setTransitionName(img,SHARE_NAME);

            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(new ChangeBounds());
            transitionSet.addTransition(new ChangeTransform());
            transitionSet.addTarget(img);
            getWindow().setSharedElementEnterTransition(transitionSet);
        }

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(new RecyclerView.Adapter<VH>() {
            @NonNull
            @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                VH vh = new VH(getLayoutInflater().inflate(R.layout.activity_animation_shareelement_target_item, parent, false));
                return vh;
            }

            @Override
            public void onBindViewHolder(@NonNull VH holder, int position) {
                Glide.with(holder.itemView.getContext()).load(pics.get(position)).into(holder.imageView);
                holder.imageView.setOnClickListener( view -> {
                    Toast.makeText(view.getContext(), "哈", Toast.LENGTH_SHORT).show();
                    ViewCompat.setTransitionName(view,SHARE_NAME+1);
                    TransitionSet transitionSet = new TransitionSet();
                    transitionSet.addTransition(new ChangeBounds());
                    transitionSet.addTransition(new ChangeTransform());
                    transitionSet.addTarget(view);
                    getWindow().setSharedElementExitTransition(transitionSet);
                });
            }

            @Override
            public int getItemCount() {
                return pics == null ? 0 : pics.size();
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
