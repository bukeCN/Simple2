package com.live.simple2.ipc.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.live.simple2.R;
import com.live.simple2.ipc.proxy.Proxy;
import com.live.simple2.ipc.server.BookManager;
import com.live.simple2.ipc.server.RemoteService;
import com.test.aidl.IMyPrintServerlInterface;

public class ClientActivity extends AppCompatActivity {

    private TextView textView;

    private BookManager bookManager;

//    private IMyPrintServerlInterface iMyPrintServerlInterface;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = Proxy.asInterface(service);
//            iMyPrintServerlInterface = IMyPrintServerlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipc);

        textView = findViewById(R.id.text);


        findViewById(R.id.qureyBtn).setOnClickListener(v -> {
//            try {
//                iMyPrintServerlInterface.printSayHi("SayHi 你好！");
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            try {
                String result = bookManager.queryBook();
                textView.setText(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
}
