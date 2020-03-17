package com.season.plugin.hookcore;

import android.content.Context;
import android.content.pm.PackageManager;

import com.season.plugin.compat.ActivityThreadCompat;
import com.season.plugin.hookcore.handle.BaseHookHandle;
import com.season.plugin.hookcore.handle.HookHandlePackageManager;
import com.season.lib.reflect.FieldUtils;
import com.season.lib.reflect.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;


/**
 * Disc: hook PackageManager拦截getPackageInfo类似请求，替换包名
 *  绑定的代理
 * @see HookHandlePackageManager
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class ProxyHookPackageManager extends BaseHookProxy {


    public ProxyHookPackageManager(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new HookHandlePackageManager(mHostContext);
    }

    public boolean enable = false;
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (enable){
            return super.invoke(proxy, method, args);
        }else{
            return method.invoke(mOldObj, args);
        }
    }

    @Override
    public void onInstall() throws Throwable {
        Object currentActivityThread = ActivityThreadCompat.currentActivityThread();
        setOldObj(FieldUtils.readField(currentActivityThread, "sPackageManager"));
        Class<?> iPmClass = mOldObj.getClass();
        List<Class<?>> interfaces = Utils.getAllInterfaces(iPmClass);
        Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
        Object newPm = Proxy.newProxyInstance(iPmClass.getClassLoader(), ifs, this);
        FieldUtils.writeField(currentActivityThread, "sPackageManager", newPm);
        PackageManager pm = mHostContext.getPackageManager();
        Object mPM = FieldUtils.readField(pm, "mPM");
        if (mPM != newPm) {
            FieldUtils.writeField(pm, "mPM", newPm);
        }
    }


    public static void fixContextPackageManager(Context context) {
        try {
            Object currentActivityThread = ActivityThreadCompat.currentActivityThread();
            Object newPm = FieldUtils.readField(currentActivityThread, "sPackageManager");
            PackageManager pm = context.getPackageManager();
            Object mPM = FieldUtils.readField(pm, "mPM");
            if (mPM != newPm) {
                FieldUtils.writeField(pm, "mPM", newPm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}