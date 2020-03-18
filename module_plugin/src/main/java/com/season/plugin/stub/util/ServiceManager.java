package com.season.plugin.stub.util;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;

import com.season.plugin.core.PluginManager;
import com.season.plugin.core.PluginProcessManager;
import com.season.plugin.compat.ActivityThreadCompat;
import com.season.plugin.compat.CompatibilityInfoCompat;
import com.season.plugin.hookcore.Env;
import com.season.lib.reflect.FieldUtils;
import com.season.lib.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Disc: 设置service的classLoader和token，确保自己完整而独立的生命周期
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-22 13:34
 */
public class ServiceManager {

    private Map<Object, Service> mTokenServices = new HashMap<Object, Service>();
    private Map<String, Service> mNameService = new HashMap<String, Service>();
    private Map<Object, Integer> mServiceTaskIds = new HashMap<Object, Integer>();

    private ServiceManager() {
    }

    private static ServiceManager sServiceManager;

    public static ServiceManager getDefault() {
        synchronized (ServiceManager.class) {
            if (sServiceManager == null) {
                sServiceManager = new ServiceManager();
            }
        }
        return sServiceManager;
    }

    public boolean hasServiceRunning() {
        return mTokenServices.size() > 0 && mNameService.size() > 0;
    }

    private Object findTokenByService(Service service) {
        for (Object s : mTokenServices.keySet()) {
            if (mTokenServices.get(s) == service) {
                return s;
            }
        }
        return null;
    }

    private void waitToFinish() {
        try {
            MethodUtils.invokeStaticMethod(Class.forName("android.app.QueuedWork"), "waitToFinish");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ClassLoader getClassLoader(ApplicationInfo pluginApplicationInfo) throws Exception {
        Object object = ActivityThreadCompat.currentActivityThread();
        if (object != null) {
            final Object obj;
            if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
                obj = MethodUtils.invokeMethod(object, "getPackageInfoNoCheck", pluginApplicationInfo, CompatibilityInfoCompat.DEFAULT_COMPATIBILITY_INFO());
            } else {
                obj = MethodUtils.invokeMethod(object, "getPackageInfoNoCheck", pluginApplicationInfo);
            }
                /*添加ClassLoader LoadedApk.mClassLoader*/
            return (ClassLoader) MethodUtils.invokeMethod(obj, "getClassLoader");
        }
        return null;
    }

    //这个需要适配,目前只是适配android api 21
    private void handleCreateServiceOne(Context hostContext, Intent stubIntent, ServiceInfo info) throws Exception {
        ResolveInfo resolveInfo = hostContext.getPackageManager().resolveService(stubIntent, 0);
        ServiceInfo stubInfo = resolveInfo != null ? resolveInfo.serviceInfo : null;
        PluginManager.getInstance().reportMyProcessName(stubInfo.processName, info.processName, info.packageName);
        PluginProcessManager.preLoadApk(hostContext, info);
        Object activityThread = ActivityThreadCompat.currentActivityThread();
        IBinder fakeToken = new ServiceTokenBinder();
        Class CreateServiceData = Class.forName(ActivityThreadCompat.activityThreadClass().getName() + "$CreateServiceData");
        Constructor init = CreateServiceData.getDeclaredConstructor();
        if (!init.isAccessible()) {
            init.setAccessible(true);
        }
        Object data = init.newInstance();

        FieldUtils.writeField(data, "token", fakeToken);
        //FieldUtils.writeField(data, "infoView", info);
        FieldUtils.writeField(data, "info", info);
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            FieldUtils.writeField(data, "compatInfo", CompatibilityInfoCompat.DEFAULT_COMPATIBILITY_INFO());
        }

        Method method = activityThread.getClass().getDeclaredMethod("handleCreateService", CreateServiceData);
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        method.invoke(activityThread, data);
        Object mService = FieldUtils.readField(activityThread, "mServices");
        Service service = (Service) MethodUtils.invokeMethod(mService, "get", fakeToken);
        MethodUtils.invokeMethod(mService, "remove", fakeToken);
        mTokenServices.put(fakeToken, service);
        mNameService.put(info.name, service);


        if (stubInfo != null) {
            PluginManager.getInstance().onServiceCreated(stubInfo, info);
        }
    }

    private void handleOnStartOne(Intent intent, int flags, int startIds) throws Exception {
        ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
        if (info != null) {
            Service service = mNameService.get(info.name);
            if (service != null) {
                ClassLoader classLoader = getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                Object token = findTokenByService(service);
                Integer integer = mServiceTaskIds.get(token);
                if (integer == null) {
                    integer = -1;
                }
                int startId = integer + 1;
                mServiceTaskIds.put(token, startId);
                int res = service.onStartCommand(intent, flags, startId);
                waitToFinish();
            }
        }
    }

    private void handleOnTaskRemovedOne(Intent intent) throws Exception {
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
            if (info != null) {
                Service service = mNameService.get(info.name);
                if (service != null) {
                    ClassLoader classLoader = getClassLoader(info.applicationInfo);
                    intent.setExtrasClassLoader(classLoader);
                    service.onTaskRemoved(intent);
                    waitToFinish();
                }
                waitToFinish();
            }
        }
    }


    private void handleOnDestroyOne(ServiceInfo targetInfo) {
        Service service = mNameService.get(targetInfo.name);
        if (service != null) {
            service.onDestroy();
            mNameService.remove(targetInfo.name);
            Object token = findTokenByService(service);
            mTokenServices.remove(token);
            mServiceTaskIds.remove(token);
            service = null;
            waitToFinish();
            PluginManager.getInstance().onServiceDestory(null, targetInfo);
        }
        waitToFinish();
    }


    private IBinder handleOnBindOne(Intent intent) throws Exception {
        ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
        if (info != null) {
            Service service = mNameService.get(info.name);
            if (service != null) {
                ClassLoader classLoader = getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                return service.onBind(intent);
            }
        }
        return null;
    }

    private void handleOnRebindOne(Intent intent) throws Exception {
        ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
        if (info != null) {
            Service service = mNameService.get(info.name);
            if (service != null) {
                ClassLoader classLoader = getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                service.onRebind(intent);
            }
        }
    }

    private boolean handleOnUnbindOne(Intent intent) throws Exception {
        ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
        if (info != null) {
            Service service = mNameService.get(info.name);
            if (service != null) {
                ClassLoader classLoader = getClassLoader(info.applicationInfo);
                intent.setExtrasClassLoader(classLoader);
                return service.onUnbind(intent);
            }
        }
        return false;
    }


    public int onStart(Context context, Intent intent, int flags, int startId) throws Exception {
        Intent targetIntent = intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if (targetIntent != null) {
            ServiceInfo targetInfo = PluginManager.getInstance().resolveServiceInfo(targetIntent, 0);
            if (targetInfo != null) {
                Service service = mNameService.get(targetInfo.name);
                if (service == null) {

                    handleCreateServiceOne(context, intent, targetInfo);
                }
                handleOnStartOne(targetIntent, flags, startId);
            }
        }
        return -1;
    }

    public void onTaskRemoved(Context context, Intent intent) throws Exception {
        Intent targetIntent = intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if (targetIntent != null) {
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(targetIntent, 0);
            Service service = mNameService.get(info.name);
            if (service == null) {
                handleCreateServiceOne(context, intent, info);
            }
            handleOnTaskRemovedOne(targetIntent);
        }
    }

    public IBinder onBind(Context context, Intent intent) throws Exception {
        Intent targetIntent = intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if (targetIntent != null) {
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(targetIntent, 0);
            Service service = mNameService.get(info.name);
            if (service == null) {
                handleCreateServiceOne(context, intent, info);
            }
            return handleOnBindOne(targetIntent);
        }
        return null;
    }

    public void onRebind(Context context, Intent intent) throws Exception {
        Intent targetIntent = intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if (targetIntent != null) {
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(targetIntent, 0);
            Service service = mNameService.get(info.name);
            if (service == null) {
                handleCreateServiceOne(context, intent, info);
            }
            handleOnRebindOne(targetIntent);
        }
    }

    public boolean onUnbind(Intent intent) throws Exception {
        Intent targetIntent = intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
        if (targetIntent != null) {
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(targetIntent, 0);
            Service service = mNameService.get(info.name);
            if (service != null) {
                return handleOnUnbindOne(targetIntent);
            }
        }
        return false;
    }


    public int stopService(Context context, Intent intent) throws Exception {
        ServiceInfo targetInfo = PluginManager.getInstance().resolveServiceInfo(intent, 0);
        if (targetInfo != null) {
            handleOnUnbindOne(intent);
            handleOnDestroyOne(targetInfo);
            return 1;
        }
        return 0;
    }

    public boolean stopServiceToken(ComponentName cn, IBinder token, int startId) throws Exception {
        Service service = mTokenServices.get(token);
        if (service != null) {
            Integer lastId = mServiceTaskIds.get(token);
            if (lastId == null) {
                return false;
            }
            if (startId != lastId) {
                return false;
            }
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceInfo info = PluginManager.getInstance().resolveServiceInfo(intent, 0);
            if (info != null) {
                handleOnUnbindOne(intent);
                handleOnDestroyOne(info);
                return true;
            }
        }
        return false;
    }

    public void onDestroy() {
        for (Service service : mTokenServices.values()) {
            service.onDestroy();
        }
        mTokenServices.clear();
        mServiceTaskIds.clear();
        mNameService.clear();
        waitToFinish();
    }
}
