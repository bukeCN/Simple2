package com.live.simple2.ipc.client;

import android.os.IBinder;
import android.os.RemoteException;

public interface IClientService {
    String DESCRIPTOR = "com.shmple2.iclent";// 唯一描述
     int TRANSACTION_printMsg = IBinder.FIRST_CALL_TRANSACTION;

    void printMsg(String msg) throws RemoteException;
}
