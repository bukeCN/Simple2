package com.live.simple2.ipc.server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.live.simple2.ipc.proxy.IClientService;
import com.test.aidl.IMyPrintServerlInterface;

/**
 * 服务提供类，继承 Service 纯粹是基于 Android 系统机制，让服务长久运行，不是必要的。必要的是 Stub.
 */
public class RemoteService extends Service  {

    private IClientService iClientService;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                iClientService.printMsg("这是来自服务端的调用。");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private Stub bookManager = new Stub() {
        @Override
        public String queryBook() {
            return "成功！";
        }

        @Override
        public void bindIClient(IBinder binder) {
            Log.e("sun","bindIClient调用");
            // 这里如何转换成 IClientService？？还是代理，

            iClientService = new IClientProxy(binder);

            handler.sendEmptyMessageDelayed(0,2000);
        }

    };

    private IMyPrintServerlInterface.Stub stub = new IMyPrintServerlInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void printSayHi(String hiMsg) throws RemoteException {
            Log.e("sun",hiMsg);
        }

        @Override
        public void printSayByby(String bybyMsg) throws RemoteException {
            Log.e("sun",bybyMsg);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("sun","onBind()");
        return bookManager;
    }

    /**
     * 客户端代理
     */
    class IClientProxy implements IClientService {
        private IBinder client;

        public IClientProxy(IBinder binder){
            this.client = binder;
        }

        @Override
        public void printMsg(String msg) throws RemoteException {
            // 数据准备
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String result = null;

            try {
                // 远程调用
                Log.e("sun", "表示是真的远程调用queryBook()!");
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(msg);
                client.transact(TRANSACTION_printMsg, data, reply, 0);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

}
