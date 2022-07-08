package com.live.simple2.ipc.proxy;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 客户端 Binder，如何传递给 Server ？
 */
public class ClientService extends Binder implements IClientService {

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
