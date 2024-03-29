package com.live.simple2.ipc.server;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 一个 Binder 对象实体，位于服务端，来用处理 Client 端的请求数据。
 * 可以对应 Native 层的 BnBinder 既 BBinder,需要实现 onTransact() 处理客户端 Proxy(对应的BpBinder) 发来的请求
 *
 */
public abstract  class Stub extends Binder implements BookManager {

    public Stub(){
        this.attachInterface(this, DESCRIPTOR);
    }

    @Override
    public IBinder asBinder() {
        return this;
    }

    /**
     * 接收到 Client 端的数据包，调用相应的接口处理请求。
     *
     * @param code
     * @param data
     * @param reply
     * @param flags
     * @return
     * @throws RemoteException
     */
    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        Log.e("sun", "表示是真的远程调用!");
        switch (code){
            case INTERFACE_TRANSACTION:
                reply.writeString(DESCRIPTOR);
                return true;
            case TRANSACTION_queryBook:
                // 表示，远程请求调用 queryBook() 函数
                data.enforceInterface(DESCRIPTOR);// 这一步？？
                // 函数调用处理
                String book = this.queryBook();
                String result = "this.queryBook()" + book;
                reply.writeNoException();
                // 写入结果
                reply.writeString(result);
                return true;
            case TRANSACTION_bindIClient:
                Log.e("sun","TRANSACTION_bindIClient");
                data.enforceInterface(DESCRIPTOR);// 这一步？？
                // 如何从 data 中获取数据。
                bindIClient(data.readStrongBinder());
                return true;
        }
        return super.onTransact(code, data, reply, flags);
    }


}
