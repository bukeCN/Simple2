package com.live.simple2.ipc.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    private IClient iClient = new IClient();


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = Proxy.asInterface(service);
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
                bookManager.bindIClient(iClient);
//                String result = bookManager.queryBook();
//                textView.setText(result);
//
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 客户端 Binder，如何传递给 Server ？
     */
    class IClient extends Binder implements IClientService {

        @Override
        public void printMsg(String msg) {
            Log.e("sun", "输出信息" + msg);
        }

        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_printMsg:
                    data.enforceInterface(DESCRIPTOR);
                    String msg = data.readString();
                    printMsg(msg);
                    return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}
