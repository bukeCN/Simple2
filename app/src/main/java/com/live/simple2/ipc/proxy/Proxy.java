package com.live.simple2.ipc.proxy;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.live.simple2.ipc.server.BookManager;
import com.live.simple2.ipc.server.Stub;

/**
 * 位于客户端的代理，对远程函数调用做包装。是对服务端 Binder 的引用。
 */
public class Proxy implements BookManager {
    private IBinder remote;

    public Proxy(IBinder iBinder){
        this.remote = iBinder;
    }

    /**
     * Binder 转换
     * @param binder
     * @return
     */
    public static BookManager asInterface(IBinder binder){
        if (binder == null) return null;
        IInterface iInterface = binder.queryLocalInterface(DESCRIPTOR);
        if (iInterface != null && iInterface instanceof BookManager){
            // 非跨进程
            return (BookManager) iInterface;
        }
        return new Proxy(binder);
    }


    @Override
    public String queryBook() throws RemoteException {
        // 数据准备
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result = null;

        try {
            // 远程调用
            Log.e("sun", "表示是真的远程调用queryBook()!");
            data.writeInterfaceToken(DESCRIPTOR);
            remote.transact(BookManager.TRANSACTION_queryBook, data, reply, 0);

            // 处理结果
            reply.readException();
            result = reply.readString();
        } finally {
            data.recycle();
            reply.recycle();
        }
        return result;
    }

    @Override
    public IBinder asBinder() {
        return remote;
    }
}
