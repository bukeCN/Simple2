package com.example.app2.ipc.client;

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

import com.example.app2.R;
import com.example.app2.ipc.proxy.BinderProxy;
import com.example.app2.ipc.proxy.ClientService;
import com.example.app2.ipc.server.BookManager;
import com.example.app2.ipc.server.RemoteService;

public class ClientActivity extends AppCompatActivity {

    private TextView textView;

    private BookManager bookManagerProxy;

    private ClientService bpService = new ClientService();


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManagerProxy = BinderProxy.asInterface(service);
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
            // 向 service 传递匿名 binder
            try {
//                bookManagerProxy.bindIClient(bpService);
                String result = bookManagerProxy.queryBook();
                textView.setText(result);
//
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


}
