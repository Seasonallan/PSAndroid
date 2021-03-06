package com.season.plugin.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;


import com.season.lib.util.LogUtil;
import com.season.plugin.hookcore.BaseHook;
import com.season.plugin.hookcore.HookHandlerCallback;
import com.season.plugin.hookcore.HookInstrumentation;
import com.season.plugin.hookcore.ProxyHookActivityManager;
import com.season.plugin.hookcore.ProxyHookActivityTaskManager;
import com.season.plugin.hookcore.ProxyHookPackageManager;
import com.season.lib.support.reflect.MethodUtils;
import com.season.pluginlib.IApplicationCallback;
import com.season.pluginlib.IPackageDataObserver;
import com.season.pluginlib.IPluginManager;

import java.util.List;

/**
 * Disc: 插件服务连接
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 09:35
 */
public class PluginManager implements ServiceConnection {

    private static final String TAG = PluginManager.class.getSimpleName();

    public static final String ACTION_PACKAGE_ADDED = "com.morgoo.doirplugin.PACKAGE_ADDED";
    public static final String ACTION_PACKAGE_REMOVED = "com.morgoo.doirplugin.PACKAGE_REMOVED";
    public static final String ACTION_SHORTCUT_PROXY = "com.morgoo.droidplugin.ACTION_SHORTCUT_PROXY";

    public static final String STUB_AUTHORITY_NAME = "com.morgoo.droidplugin_stub";
    public static final String EXTRA_APP_PERSISTENT = "com.morgoo.droidplugin.EXTRA_APP_PERSISTENT";

    public static final int STUB_NO_ACTIVITY_MAX_NUM = 4;


    private Context mHostContext;
    private static PluginManager sInstance = null;

    public static PluginManager getInstance() {
        if (sInstance == null) {
            sInstance = new PluginManager();
        }
        return sInstance;
    }

    private IPluginManager mPluginManager;
    private ProxyHookPackageManager mIPackageManagerHook;


    /**
     * 绑定服务，启动hook代理
     * @param hostContext
     */
    public void bindPluginManagerToService(Context hostContext) {
        this.mHostContext = hostContext;
        //hook PackageManager 拦截getPackageInfo类似请求，替换包名,使用动态代理
        mIPackageManagerHook = new ProxyHookPackageManager(mHostContext);
        installHookOnce(mIPackageManagerHook);

        //hook ActivityManager 拦截startActivity类似请求，替换intent，绕过AndroidManifest检测 使用动态代理
        installHookOnce(new ProxyHookActivityManager(mHostContext));

        //hook Handler的Callback， 在假的activity启动后替换为原本需要的activity并启动它 使用静态代理
        installHookOnce(new HookHandlerCallback(mHostContext));

        //hook Instrumentation 处理生命周期，替身的创建和销毁，并伪装系统服务 使用静态代理
        installHookOnce(new HookInstrumentation(mHostContext));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            installHookOnce(new ProxyHookActivityTaskManager(mHostContext));
        }
        connectToService();
    }

    private void installHookOnce(BaseHook hook) {
        try {
            hook.onInstall();
        } catch (Throwable throwable) {
             LogUtil.e(TAG, hook.toString());
        }
    }

    @Override
    public void onServiceConnected(final ComponentName componentName, IBinder iBinder) {
        mPluginManager = IPluginManager.Stub.asInterface(iBinder);
        try {
            mPluginManager.waitForReady();
            //开始使用hook过的代理
            mIPackageManagerHook.enable = true;
            mPluginManager.registerApplicationCallback(new IApplicationCallback.Stub() {

                @Override
                public Bundle onCallback(Bundle extra) throws RemoteException {
                    return extra;
                }
            });

            mPluginManager.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    onServiceDisconnected(componentName);
                }
            }, 0);

            Log.i(TAG, "PluginManager ready!");
        } catch (Throwable e) {
            Log.e(TAG, "Lost the mPluginManager connect...", e);
        } finally {
            try {
                synchronized (mWaitLock) {
                    mWaitLock.notifyAll();
                }
            } catch (Exception e) {
                Log.i(TAG, "PluginManager notifyAll:" + e.getMessage());
            }
        }
    }

    /**
     * 服务是否已经连接
     * @return
     */
    public boolean isConnected() {
        return mHostContext != null && mPluginManager != null;
    }

    private Object mWaitLock = new Object();

    public void waitForConnected() {
        if (isConnected()) {
            return;
        } else {
            try {
                synchronized (mWaitLock) {
                    mWaitLock.wait();
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "waitForConnected:" + e.getMessage());
            }
            Log.i(TAG, "waitForConnected finish");
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mPluginManager = null;
        //服务连接断开，需要重新连接。
        connectToService();
    }

    private void connectToService() {
        if (mPluginManager == null) {
            try {
                Intent intent = new Intent(mHostContext, PluginManagerService.class);
                intent.setPackage(mHostContext.getPackageName());
                mHostContext.startService(intent);

                mHostContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                Log.e(TAG, "connectToService", e);
            }
        }
    }

    public Context getHostContext() {
        return mHostContext;
    }


    public PackageInfo getPackageInfo(String packageName, int flags){
        try {
            if (mPluginManager != null) {
                return mPluginManager.getPackageInfo(packageName, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (Exception e) {
            Log.e(TAG, "getPackageInfo", e);
        }
        return null;
    }


    public void clearApplicationUserData(String packageName, final Object observer/*android.content.pm.IPackageDataObserver*/) throws RemoteException {
        try {
            if (mPluginManager != null && packageName != null) {
                mPluginManager.clearApplicationUserData(packageName, new IPackageDataObserver.Stub() {

                    @Override
                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                        if (observer != null) {
                            try {
                                MethodUtils.invokeMethod(observer, "onRemoveCompleted", packageName, succeeded);
                            } catch (Exception e) {
                                RemoteException exception = new RemoteException();
                                exception.initCause(exception);
                                throw exception;
                            }
                        }
                    }
                });
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "clearApplicationUserData", e);
        }
    }

    public boolean isPluginPackage(String packageName) throws RemoteException {
        try {
            if (mHostContext == null) {
                return false;
            }
            if (TextUtils.equals(mHostContext.getPackageName(), packageName)) {
                return false;
            }

            if (mPluginManager != null && packageName != null) {
                return mPluginManager.isPluginPackage(packageName);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "isPluginPackage", e);
        }
        return false;
    }

    public boolean isPluginPackage(ComponentName className) throws RemoteException {
        if (className == null) {
            return false;
        }
        return isPluginPackage(className.getPackageName());
    }

    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {

        try {
            if (className == null) {
                return null;
            }
            if (mPluginManager != null && className != null) {
                return mPluginManager.getActivityInfo(className, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getActivityInfo RemoteException", e);
        } catch (Exception e) {
            Log.e(TAG, "getActivityInfo", e);
        }
        return null;
    }

    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {
        if (className == null) {
            return null;
        }
        try {
            if (mPluginManager != null && className != null) {
                return mPluginManager.getReceiverInfo(className, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getReceiverInfo", e);
        }
        return null;
    }

    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {
        if (className == null) {
            return null;
        }
        try {
            if (mPluginManager != null && className != null) {
                return mPluginManager.getServiceInfo(className, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getServiceInfo", e);
        }
        return null;
    }

    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {
        if (className == null) {
            return null;
        }
        try {
            if (mPluginManager != null && className != null) {
                return mPluginManager.getProviderInfo(className, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getProviderInfo", e);
        }
        return null;
    }

    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.resolveIntent(intent, resolvedType, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "resolveIntent", e);
        }
        return null;
    }

    public ResolveInfo resolveService(Intent intent, String resolvedType, Integer flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.resolveService(intent, resolvedType, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "resolveService", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentActivities(intent, resolvedType, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "queryIntentActivities RemoteException", e);
        } catch (Exception e) {
            Log.e(TAG, "queryIntentActivities", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentReceivers(intent, resolvedType, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "queryIntentReceivers", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentServices(intent, resolvedType, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "queryIntentServices RemoteException", e);
        } catch (Exception e) {
            Log.e(TAG, "queryIntentServices", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentContentProviders(intent, resolvedType, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "queryIntentContentProviders", e);
        }
        return null;
    }

    public List<PackageInfo> getInstalledPackages(int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getInstalledPackages(flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getInstalledPackages RemoteException", e);
        } catch (Exception e) {
            Log.e(TAG, "getInstalledPackages", e);
        }
        return null;
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {

        try {
            if (mPluginManager != null) {
                return mPluginManager.getInstalledApplications(flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getInstalledApplications", e);
        }
        return null;
    }

    public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && name != null) {
                return mPluginManager.getPermissionInfo(name, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getPermissionInfo", e);
        }
        return null;
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && group != null) {
                return mPluginManager.queryPermissionsByGroup(group, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "queryPermissionsByGroup", e);
        }
        return null;
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && name != null) {
                return mPluginManager.getPermissionGroupInfo(name, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getPermissionGroupInfo", e);
        }
        return null;
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getAllPermissionGroups(flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getAllPermissionGroups", e);
        }
        return null;
    }

    public ProviderInfo resolveContentProvider(String name, Integer flags) throws RemoteException {
        try {
            if (mPluginManager != null && name != null) {
                return mPluginManager.resolveContentProvider(name, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "resolveContentProvider", e);
        }
        return null;
    }


    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && packageName != null) {
                return mPluginManager.getApplicationInfo(packageName, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getApplicationInfo RemoteException", e);
        } catch (Exception e) {
            Log.e(TAG, "getApplicationInfo", e);
        }
        return null;
    }

    public ActivityInfo selectStubActivityInfo(ActivityInfo pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubActivityInfo(pluginInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "selectStubActivityInfo", e);
        }
        return null;
    }

    public ActivityInfo selectStubActivityInfo(Intent pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubActivityInfoByIntent(pluginInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "selectStubActivityInfo", e);
        }
        return null;
    }

    public ServiceInfo selectStubServiceInfo(ServiceInfo pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubServiceInfo(pluginInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "selectStubServiceInfo", e);
        }
        return null;
    }

    public ServiceInfo selectStubServiceInfo(Intent pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubServiceInfoByIntent(pluginInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "selectStubServiceInfo", e);
        }
        return null;
    }

    public ProviderInfo selectStubProviderInfo(String name) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubProviderInfo(name);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "selectStubProviderInfo", e);
        }
        return null;
    }

    public ActivityInfo resolveActivityInfo(Intent intent, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                if (intent.getComponent() != null) {
                    return mPluginManager.getActivityInfo(intent.getComponent(), flags);
                } else {
                    ResolveInfo resolveInfo = mPluginManager.resolveIntent(intent, intent.resolveTypeIfNeeded(mHostContext.getContentResolver()), flags);
                    if (resolveInfo != null && resolveInfo.activityInfo != null) {
                        return resolveInfo.activityInfo;
                    }
                }
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
            return null;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "selectStubActivityInfo", e);
        }
        return null;
    }

    public ServiceInfo resolveServiceInfo(Intent intent, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                if (intent.getComponent() != null) {
                    return mPluginManager.getServiceInfo(intent.getComponent(), flags);
                } else {
                    ResolveInfo resolveInfo = mPluginManager.resolveIntent(intent, intent.resolveTypeIfNeeded(mHostContext.getContentResolver()), flags);
                    if (resolveInfo != null && resolveInfo.serviceInfo != null) {
                        return resolveInfo.serviceInfo;
                    }
                }
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
            return null;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "resolveServiceInfo", e);
        }
        return null;
    }

    public void killBackgroundProcesses(String packageName) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.killBackgroundProcesses(packageName);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "killBackgroundProcesses", e);
        }
    }

    public void forceStopPackage(String packageName) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.forceStopPackage(packageName);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "forceStopPackage", e);
        }

    }

    public boolean killApplicationProcess(String packageName) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.killApplicationProcess(packageName);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "killApplicationProcess", e);
        }
        return false;
    }

    public List<ActivityInfo> getReceivers(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getReceivers(packageName, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getReceivers", e);
        }
        return null;
    }

    public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getReceiverIntentFilter(info);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getReceiverIntentFilter", e);
        }
        return null;
    }

    public ServiceInfo getTargetServiceInfo(ServiceInfo info) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getTargetServiceInfo(info);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getTargetServiceInfo", e);
        }
        return null;
    }

    public int installPackage(String filepath, int flags){
        try {
            if (mPluginManager != null) {
                int result = mPluginManager.installPackage(filepath, flags);
                Log.w(TAG, String.format("%s install result %d", filepath, result));
                return result;
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (Exception e) {
            Log.e(TAG, "forceStopPackage", e);
        }
        return -1;
    }

    public List<String> getPackageNameByPid(int pid) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getPackageNameByPid(pid);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "forceStopPackage", e);
        }
        return null;
    }


    public String getProcessNameByPid(int pid) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getProcessNameByPid(pid);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "forceStopPackage", e);
        }
        return null;
    }

    public void onActivityCreated(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onActivityCreated(stubInfo, targetInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "onActivityCreated", e);
        }
    }

    public void onActivityDestory(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onActivityDestory(stubInfo, targetInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "onActivityDestory", e);
        }
    }

    public void onServiceCreated(ServiceInfo stubInfo, ServiceInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onServiceCreated(stubInfo, targetInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "onServiceCreated", e);
        }
    }


    public void onServiceDestory(ServiceInfo stubInfo, ServiceInfo targetInfo) {
        try {
            if (mPluginManager != null) {
                mPluginManager.onServiceDestory(stubInfo, targetInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (Exception e) {
            Log.e(TAG, "onServiceDestory", e);
        }
    }

    public void onProviderCreated(ProviderInfo stubInfo, ProviderInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onProviderCreated(stubInfo, targetInfo);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "onProviderCreated", e);
        }
    }

    public void reportMyProcessName(String stubProcessName, String targetProcessName, String targetPkg) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.reportMyProcessName(stubProcessName, targetProcessName, targetPkg);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "reportMyProcessName", e);
        }
    }

    public int deletePackage(String packageName, int flags){
        try {
            if (mPluginManager != null) {
                return mPluginManager.deletePackage(packageName, flags);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (Exception e) {
            Log.e(TAG, "deletePackage", e);
        }
        return -1;
    }


    public int checkSignatures(String pkg0, String pkg1) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.checkSignatures(pkg0, pkg1);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
                return PackageManager.SIGNATURE_NO_MATCH;
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "deletePackage", e);
            return PackageManager.SIGNATURE_NO_MATCH;
        }
    }

    public void onActivtyOnNewIntent(ActivityInfo stubInfo, ActivityInfo targetInfo, Intent intent) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onActivtyOnNewIntent(stubInfo, targetInfo, intent);
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "onActivtyOnNewIntent", e);
        }
    }

    public int getMyPid() throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getMyPid();
            } else {
                Log.w(TAG, "Plugin Package Manager Service not be connect");
                return -1;
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "getMyPid", e);
            return -1;
        }
    }
}
