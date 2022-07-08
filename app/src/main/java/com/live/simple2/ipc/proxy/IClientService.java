package com.live.simple2.ipc.proxy;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * 匿名 Binder ,Client 传递给 Service，Service 进行调用。
 */
public interface IClientService {
    String DESCRIPTOR = "com.shmple2.iclent";// 唯一描述
     int TRANSACTION_printMsg = IBinder.FIRST_CALL_TRANSACTION;

    void printMsg(String msg) throws RemoteException;
}
