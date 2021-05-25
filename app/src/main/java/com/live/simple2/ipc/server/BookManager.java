package com.live.simple2.ipc.server;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/**
 * 服务端接口，定义封装了 Server 所有功能，需要 Server 和 Proxy 实现。
 */
public interface BookManager extends IInterface {
    String DESCRIPTOR = "com.shmple2.bookmanager";// 唯一描述

    int TRANSACTION_queryBook = IBinder.FIRST_CALL_TRANSACTION;

    String queryBook() throws RemoteException;
}
