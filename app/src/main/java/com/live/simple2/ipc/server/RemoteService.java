package com.live.simple2.ipc.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.test.aidl.IMyPrintServerlInterface;

/**
 * 服务提供类，继承 Service 纯粹是基于 Android 系统机制，让服务长久运行，不是必要的。必要的是 Stub.
 */
public class RemoteService extends Service  {

    private Stub bookManager = new Stub() {
        @Override
        public String queryBook() {
            return "成功！";
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

}
