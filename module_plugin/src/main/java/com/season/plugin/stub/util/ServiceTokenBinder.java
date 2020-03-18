package com.season.plugin.stub.util;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.FileDescriptor;


/**
 * Disc: ServiceTokenBinder
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-22 13:34
 */
public class ServiceTokenBinder implements IBinder {

    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return null;
    }

    @Override
    public boolean pingBinder() {
        return false;
    }

    @Override
    public boolean isBinderAlive() {
        return false;
    }

    @Override
    public IInterface queryLocalInterface(String s) {
        return null;
    }

    @Override
    public void dump(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {

    }

    @Override
    public void dumpAsync(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {

    }

    @Override
    public boolean transact(int i, Parcel parcel, Parcel parcel1, int i1) throws RemoteException {
        return false;
    }

    @Override
    public void linkToDeath(DeathRecipient deathRecipient, int i) throws RemoteException {

    }

    @Override
    public boolean unlinkToDeath(DeathRecipient deathRecipient, int i) {
        return false;
    }
}
