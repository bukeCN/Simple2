package com.live.simple2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
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


        infoTv = findViewById(R.id.infoTv);

        findViewById(R.id.getAppInfosBtn).setOnClickListener( v -> {
            // 获取安装的 App 信息
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> apps = getPackageManager().queryIntentActivities(intent, 0);
            infoTv.setText(apps.toString());

            ResolveInfo app = apps.get(new Random().nextInt(apps.size()-1));
            // 获取该应用的包名
            String packageName = app.activityInfo.packageName;
            // 获取该应用的主 Activity 全限定类名
            String className = app.activityInfo.name;

            // 利用包名和类名启动 App
            ComponentName component = new ComponentName(packageName, className);
            Intent startIntent = new Intent();
//            startIntent.setComponent(component);
            startActivity(startIntent);

        });

    }
}
