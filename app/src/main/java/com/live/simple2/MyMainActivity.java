package com.live.simple2;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Random;


public class MyMainActivity extends AppCompatActivity {
    private TextView infoTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_main_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


        infoTv = findViewById(R.id.infoTv);

        findViewById(R.id.getAppInfosBtn).setOnClickListener( v -> {

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = 400;
            layoutParams.height = 400;
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;

            TextView textView = new TextView(this);
            textView.setBackgroundColor(Color.RED);
            textView.setText("测试测试测试");

            WindowManager windowManager = getWindowManager();
            windowManager.addView(textView,layoutParams);

            infoTv.postDelayed(() ->{
                Log.e("sun",textView.getWindowToken()+"**"+infoTv.getWindowToken());
            },3000);

//            PopupWindow popupWindow = new PopupWindow();
//            popupWindow.showAsDropDown();
            // 获取安装的 App 信息
//            Intent intent = new Intent(Intent.ACTION_MAIN, null);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            List<ResolveInfo> apps = getPackageManager().queryIntentActivities(intent, 0);
//            infoTv.setText(apps.toString());
//
//            ResolveInfo app = apps.get(new Random().nextInt(apps.size()-1));
//            // 获取该应用的包名
//            String packageName = app.activityInfo.packageName;
//            // 获取该应用的主 Activity 全限定类名
//            String className = app.activityInfo.name;
//
//            // 利用包名和类名启动 App
//            ComponentName component = new ComponentName(packageName, className);
//            Intent startIntent = new Intent();
////            startIntent.setComponent(component);
//            startActivity(startIntent);

        });



    }
}
