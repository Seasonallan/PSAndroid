package com.season.example.transfer.receiver;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @brief 网络状态接收者
 * @author Join
 */
public class NetworkReceiver extends BroadcastReceiver { 

    private static Map<Context, NetworkReceiver> mReceiverMap = new HashMap<Context, NetworkReceiver>();

    private OnNetworkListener mListener;

    public NetworkReceiver(OnNetworkListener listener) {
        mListener = listener;
    }

    /**
     * 注册
     */
    public static void register(Context context, OnNetworkListener listener) {
        if (mReceiverMap.containsKey(context)) { 
            return;
        }

        NetworkReceiver receiver = new NetworkReceiver(listener);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);

        mReceiverMap.put(context, receiver); 
    }

    /**
     * 注销
     */
    public static void unregister(Context context) {
        NetworkReceiver receiver = mReceiverMap.remove(context);
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null; 
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo(); 
        if (info != null) {
            boolean isWifi = info.getType() == ConnectivityManager.TYPE_WIFI;
            if (mListener != null) {
                mListener.onConnected(isWifi);
            }
        } else {
            if (mListener != null) {
                mListener.onDisconnected();
            }
        }

    }
}
