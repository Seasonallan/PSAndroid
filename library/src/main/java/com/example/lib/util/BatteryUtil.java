package com.example.lib.util;

import android.content.Context;
import android.os.BatteryManager;

import static android.content.Context.BATTERY_SERVICE;

/**
 * 电池数据
 */
public class BatteryUtil {

    /**
     * 获取当前电池百分比
     * @param context
     * @return
     */
    public static int getCurrentBattery(Context context){
        BatteryManager batteryManager = (BatteryManager)context.getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return 100;
    }
}
