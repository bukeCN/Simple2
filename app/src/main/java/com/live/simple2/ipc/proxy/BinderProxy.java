package com.live.simple2.ipc.proxy;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.live.simple2.ipc.server.BookManager;

/**
 * 位于客户端的代理，对远程函数调用做包装。是对服务端 Binder 的引用。
 */
public class BinderProxy implements BookManager {
    private IBinder remote;

    public BinderProxy(IBinder iBinder){
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
        if (iInterface instanceof BookManager){
            // 非跨进程
            return (BookManager) iInterface;
        }
        return new BinderProxy(binder);
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
            // 将数据打包，交给 BpBidner 处理（Java 层看 BidnerProxy），通讯层处理, 此处会阻塞线程
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
    public void bindIClient(IBinder binder) throws RemoteException {

        // 数据准备
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result = null;

        try {
            // 远程调用
            Log.e("sun", "表示是真的远程调用queryBook()!");
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(binder);
            remote.transact(BookManager.TRANSACTION_bindIClient, data, reply, 0);
            // 处理结果
//            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override
    public IBinder asBinder() {
        return remote;
    }
}
