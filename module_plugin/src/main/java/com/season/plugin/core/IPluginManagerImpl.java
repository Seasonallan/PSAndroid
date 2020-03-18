package com.season.plugin.core;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;


import com.season.lib.util.LogUtil;
import com.season.plugin.PluginCodeDefine;
import com.season.pluginlib.IApplicationCallback;
import com.season.pluginlib.IPackageDataObserver;
import com.season.pluginlib.IPluginManager;
import com.season.plugin.tool.IntentResolveHelper;
import com.season.plugin.parser.PluginPackageParser;
import com.season.plugin.tool.SoFileHelper;
import com.season.plugin.stub.util.ProcessManager;
import com.season.plugin.tool.PluginFileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 此服务模仿系统的PackageManagerService，提供对插件简单的管理服务。
 */
public class IPluginManagerImpl extends IPluginManager.Stub {

    private static final String TAG = IPluginManagerImpl.class.getSimpleName();

    private Map<String, PluginPackageParser> mPluginCache = Collections.synchronizedMap(new HashMap<String, PluginPackageParser>(20));

    private Context mContext;

    private AtomicBoolean mHasLoadedOk = new AtomicBoolean(false);
    private final Object mLock = new Object();

    private ProcessManager mActivityManagerService;

    private Set<String> mHostRequestedPermission = new HashSet<String>(10);

    private Map<String, Signature[]> mSignatureCache = new HashMap<String, Signature[]>();

    public IPluginManagerImpl(Context context) {
        mContext = context;
        mActivityManagerService = new ProcessManager(mContext);
    }


    public void onCreate() {
        new Thread() {
            @Override
            public void run() {
                onCreateInner();
            }
        }.start();
    }

    private void onCreateInner() {
        loadAllPlugin(mContext);
        loadHostRequestedPermission();
        try {
            mHasLoadedOk.set(true);
            synchronized (mLock) {
                mLock.notifyAll();
            }
        } catch (Exception e) {
        }
    }

    public void loadPluginsInThread(final Runnable runnable) {
        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                loadAllPlugin(mContext);
                loadHostRequestedPermission();
                try {
                    mHasLoadedOk.set(true);
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                } catch (Exception e) {
                }
                if (runnable != null){
                    handler.post(runnable);
                }
            }
        }.start();
    }

    private void loadHostRequestedPermission() {
        try {
            mHostRequestedPermission.clear();
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pms = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (pms != null && pms.requestedPermissions != null && pms.requestedPermissions.length > 0) {
                for (String requestedPermission : pms.requestedPermissions) {
                    mHostRequestedPermission.add(requestedPermission);
                }
            }
        } catch (Exception e) {
        }
    }


    private void loadAllPlugin(Context context) {
        long b = System.currentTimeMillis();
        ArrayList<File> apkfiles = null;
        try {
            apkfiles = new ArrayList<File>();
            File baseDir = new File(PluginFileHelper.getBaseDir(context));
            File[] dirs = baseDir.listFiles();
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    File file = new File(dir, "apk/base-1.apk");
                    if (file.exists()) {
                        apkfiles.add(file);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "scan a apk file error", e);
        }

        LogUtil.i(TAG, "Search apk cost %s ms", (System.currentTimeMillis() - b));
        b = System.currentTimeMillis();

        if (apkfiles != null && apkfiles.size() > 0) {
            for (File pluginFile : apkfiles) {
                long b1 = System.currentTimeMillis();
                try {
                    PluginPackageParser pluginPackageParser = new PluginPackageParser(mContext, pluginFile);
                    Signature[] signatures = readSignatures(pluginPackageParser.getPackageName());
                    if (signatures == null || signatures.length <= 0) {
                        pluginPackageParser.collectCertificates(0);
                        PackageInfo info = pluginPackageParser.getPackageInfo(PackageManager.GET_SIGNATURES);
                        saveSignatures(info);
                    } else {
                        mSignatureCache.put(pluginPackageParser.getPackageName(), signatures);
                        pluginPackageParser.writeSignature(signatures);
                    }
                    if (!mPluginCache.containsKey(pluginPackageParser.getPackageName())) {
                        mPluginCache.put(pluginPackageParser.getPackageName(), pluginPackageParser);
                    }
                } catch (Throwable e) {
                    LogUtil.i(TAG, "parse a apk file error %s", e, pluginFile.getPath());
                } finally {
                    LogUtil.i(TAG, "Parse %s apk cost %s ms", pluginFile.getPath(), (System.currentTimeMillis() - b1));
                }
            }
        }

        LogUtil.i(TAG, "Parse all apk cost %s ms", (System.currentTimeMillis() - b));
        b = System.currentTimeMillis();

        try {
            mActivityManagerService.onCreate(IPluginManagerImpl.this);
        } catch (Throwable e) {
            LogUtil.e(TAG, "mActivityManagerService.onCreate", e);
        }

        LogUtil.i(TAG, "ActivityManagerService.onCreate %s ms", (System.currentTimeMillis() - b));
    }

    private void enforcePluginFileExists() throws RemoteException {
        List<String> removedPkg = new ArrayList<>();
        for (String pkg : mPluginCache.keySet()) {
            PluginPackageParser parser = mPluginCache.get(pkg);
            File pluginFile = parser.getPluginFile();
            if (pluginFile != null && pluginFile.exists()) {
                //DO NOTHING
            } else {
                removedPkg.add(pkg);
            }
        }
        for (String pkg : removedPkg) {
            deletePackage(pkg, 0);
        }
    }


    @Override
    public boolean waitForReady() {
        waitForReadyInner();
        return true;
    }

    private void waitForReadyInner() {
        if (!mHasLoadedOk.get()) {
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }


    private void handleException(Exception e) throws RemoteException {
        RemoteException remoteException;
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            remoteException = new RemoteException(e.getMessage());
            remoteException.initCause(e);
            remoteException.setStackTrace(e.getStackTrace());
        } else {
            remoteException = new RemoteException();
            remoteException.initCause(e);
            remoteException.setStackTrace(e.getStackTrace());
        }
        throw remoteException;
    }


    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            String pkg = getAndCheckCallingPkg(packageName);
            if (pkg != null && !TextUtils.equals(packageName, mContext.getPackageName())) {
                enforcePluginFileExists();
                PluginPackageParser parser = mPluginCache.get(pkg);
                if (parser != null) {
                    PackageInfo packageInfo = parser.getPackageInfo(flags);
                    if (packageInfo != null && (flags & PackageManager.GET_SIGNATURES) != 0 && packageInfo.signatures == null) {
                        packageInfo.signatures = mSignatureCache.get(packageName);
                    }
                    return packageInfo;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }


    @Override
    public boolean isPluginPackage(String packageName) throws RemoteException {
        waitForReadyInner();
        enforcePluginFileExists();
        return mPluginCache.containsKey(packageName);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            String pkg = getAndCheckCallingPkg(className.getPackageName());
            if (pkg != null) {
                enforcePluginFileExists();
                PluginPackageParser parser = mPluginCache.get(className.getPackageName());
                if (parser != null) {
                    return parser.getActivityInfo(className, flags);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            String pkg = getAndCheckCallingPkg(className.getPackageName());
            if (pkg != null) {
                enforcePluginFileExists();
                PluginPackageParser parser = mPluginCache.get(className.getPackageName());
                if (parser != null) {
                    return parser.getReceiverInfo(className, flags);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            String pkg = getAndCheckCallingPkg(className.getPackageName());
            if (pkg != null) {
                enforcePluginFileExists();
                PluginPackageParser parser = mPluginCache.get(className.getPackageName());
                if (parser != null) {
                    return parser.getServiceInfo(className, flags);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }

        return null;
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            String pkg = getAndCheckCallingPkg(className.getPackageName());
            if (pkg != null) {
                enforcePluginFileExists();
                PluginPackageParser parser = mPluginCache.get(className.getPackageName());
                if (parser != null) {
                    return parser.getProviderInfo(className, flags);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    private boolean shouldNotBlockOtherInfo() {
        return true;
//        int pid = Binder.getCallingPid();
//        if (pid == android.os.Process.myPid()) {
//            return true;
//        } else {
//            List<String> pkgs = mActivityManagerService.getPackageNamesByPid(pid);
//            if (pkgs != null && pkgs.size() > 0 && !pkgs.contains(mContext.getPackageName())) {
//                return false;
//            } else {
//                return true;
//            }
//        }
    }

    private String getAndCheckCallingPkg(String pkg) {
        return pkg;
//        if (shouldNotBlockOtherInfo()) {
//            return pkg;
//        } else {
//            if (!pkgInPid(Binder.getCallingPid(), pkg)) {
//                return null;
//            } else {
//                return pkg;
//            }
//        }
    }

    private boolean pkgInPid(int pid, String pkg) {
        List<String> pkgs = mActivityManagerService.getPackageNamesByPid(pid);
        if (pkgs != null && pkgs.size() > 0) {
            return pkgs.contains(pkg);
        } else {
            return true;
        }
    }

    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                List<ResolveInfo> infos = IntentResolveHelper.resolveIntent(mContext, mPluginCache, intent, resolvedType, flags);
                if (infos != null && infos.size() > 0) {
                    return IntentResolveHelper.findBest(infos);
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                List<ResolveInfo> infos = new ArrayList<ResolveInfo>();
                for (String pkg : pkgs) {
                    intent.setPackage(pkg);
                    List<ResolveInfo> list = IntentResolveHelper.resolveIntent(mContext, mPluginCache, intent, resolvedType, flags);
                    infos.addAll(list);
                }
                if (infos != null && infos.size() > 0) {
                    return IntentResolveHelper.findBest(infos);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }


    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                return IntentResolveHelper.resolveActivityIntent(mContext, mPluginCache, intent, resolvedType, flags);
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                List<ResolveInfo> infos = new ArrayList<ResolveInfo>();
                for (String pkg : pkgs) {
                    intent.setPackage(pkg);
                    List<ResolveInfo> list = IntentResolveHelper.resolveActivityIntent(mContext, mPluginCache, intent, resolvedType, flags);
                    infos.addAll(list);
                }
                if (infos != null && infos.size() > 0) {
                    return infos;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                return IntentResolveHelper.resolveReceiverIntent(mContext, mPluginCache, intent, resolvedType, flags);
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                List<ResolveInfo> infos = new ArrayList<ResolveInfo>();
                for (String pkg : pkgs) {
                    intent.setPackage(pkg);
                    List<ResolveInfo> list = IntentResolveHelper.resolveReceiverIntent(mContext, mPluginCache, intent, resolvedType, flags);
                    infos.addAll(list);
                }
                if (infos != null && infos.size() > 0) {
                    return infos;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                List<ResolveInfo> infos = IntentResolveHelper.resolveServiceIntent(mContext, mPluginCache, intent, resolvedType, flags);
                if (infos != null && infos.size() > 0) {
                    return IntentResolveHelper.findBest(infos);
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                List<ResolveInfo> infos = new ArrayList<ResolveInfo>();
                for (String pkg : pkgs) {
                    intent.setPackage(pkg);
                    List<ResolveInfo> list = IntentResolveHelper.resolveServiceIntent(mContext, mPluginCache, intent, resolvedType, flags);
                    infos.addAll(list);
                }
                if (infos != null && infos.size() > 0) {
                    return IntentResolveHelper.findBest(infos);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                return IntentResolveHelper.resolveServiceIntent(mContext, mPluginCache, intent, resolvedType, flags);
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                List<ResolveInfo> infos = new ArrayList<ResolveInfo>();
                for (String pkg : pkgs) {
                    intent.setPackage(pkg);
                    List<ResolveInfo> list = IntentResolveHelper.resolveServiceIntent(mContext, mPluginCache, intent, resolvedType, flags);
                    infos.addAll(list);
                }
                if (infos != null && infos.size() > 0) {
                    return infos;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                return IntentResolveHelper.resolveProviderIntent(mContext, mPluginCache, intent, resolvedType, flags);
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                List<ResolveInfo> infos = new ArrayList<ResolveInfo>();
                for (String pkg : pkgs) {
                    intent.setPackage(pkg);
                    List<ResolveInfo> list = IntentResolveHelper.resolveProviderIntent(mContext, mPluginCache, intent, resolvedType, flags);
                    infos.addAll(list);
                }
                if (infos != null && infos.size() > 0) {
                    return infos;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }


    @Override
    public List<PackageInfo> getInstalledPackages(int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            List<PackageInfo> infos = new ArrayList<PackageInfo>(mPluginCache.size());
            if (shouldNotBlockOtherInfo()) {
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    infos.add(pluginPackageParser.getPackageInfo(flags));
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    if (pkgs.contains(pluginPackageParser.getPackageName())) {
                        infos.add(pluginPackageParser.getPackageInfo(flags));
                    }
                }
            }
            return infos;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            List<ApplicationInfo> infos = new ArrayList<ApplicationInfo>(mPluginCache.size());
            if (shouldNotBlockOtherInfo()) {
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    infos.add(pluginPackageParser.getApplicationInfo(flags));
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    if (pkgs.contains(pluginPackageParser.getPackageName())) {
                        infos.add(pluginPackageParser.getApplicationInfo(flags));
                    }
                }

            }
            return infos;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionInfo> permissionInfos = pluginPackageParser.getPermissions();
                    for (PermissionInfo permissionInfo : permissionInfos) {
                        if (TextUtils.equals(permissionInfo.name, name)) {
                            return permissionInfo;
                        }
                    }
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionInfo> permissionInfos = pluginPackageParser.getPermissions();
                    for (PermissionInfo permissionInfo : permissionInfos) {
                        if (TextUtils.equals(permissionInfo.name, name) && pkgs.contains(permissionInfo.packageName)) {
                            return permissionInfo;
                        }
                    }
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            List<PermissionInfo> list = new ArrayList<PermissionInfo>();
            if (shouldNotBlockOtherInfo()) {
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionInfo> permissionInfos = pluginPackageParser.getPermissions();
                    for (PermissionInfo permissionInfo : permissionInfos) {
                        if (TextUtils.equals(permissionInfo.group, group) && !list.contains(permissionInfo)) {
                            list.add(permissionInfo);
                        }
                    }
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionInfo> permissionInfos = pluginPackageParser.getPermissions();
                    for (PermissionInfo permissionInfo : permissionInfos) {
                        if (pkgs.contains(permissionInfo.packageName) && TextUtils.equals(permissionInfo.group, group) && !list.contains(permissionInfo)) {
                            list.add(permissionInfo);
                        }
                    }
                }
            }
            return list;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionGroupInfo> permissionGroupInfos = pluginPackageParser.getPermissionGroups();
                    for (PermissionGroupInfo permissionGroupInfo : permissionGroupInfos) {
                        if (TextUtils.equals(permissionGroupInfo.name, name)) {
                            return permissionGroupInfo;
                        }
                    }
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionGroupInfo> permissionGroupInfos = pluginPackageParser.getPermissionGroups();
                    for (PermissionGroupInfo permissionGroupInfo : permissionGroupInfos) {
                        if (TextUtils.equals(permissionGroupInfo.name, name) && pkgs.contains(permissionGroupInfo.packageName)) {
                            return permissionGroupInfo;
                        }
                    }
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            List<PermissionGroupInfo> list = new ArrayList<PermissionGroupInfo>();
            if (shouldNotBlockOtherInfo()) {
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionGroupInfo> permissionGroupInfos = pluginPackageParser.getPermissionGroups();
                    for (PermissionGroupInfo permissionGroupInfo : permissionGroupInfos) {
                        if (!list.contains(permissionGroupInfo)) {
                            list.add(permissionGroupInfo);
                        }
                    }
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<PermissionGroupInfo> permissionGroupInfos = pluginPackageParser.getPermissionGroups();
                    for (PermissionGroupInfo permissionGroupInfo : permissionGroupInfos) {
                        if (!list.contains(permissionGroupInfo) && pkgs
                                .contains(permissionGroupInfo.packageName)) {
                            list.add(permissionGroupInfo);
                        }
                    }
                }
            }
            return list;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            enforcePluginFileExists();
            if (shouldNotBlockOtherInfo()) {
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<ProviderInfo> providerInfos = pluginPackageParser.getProviders();
                    for (ProviderInfo providerInfo : providerInfos) {
                        if (TextUtils.equals(providerInfo.authority, name)) {
                            return providerInfo;
                        }
                    }
                }
            } else {
                List<String> pkgs = mActivityManagerService.getPackageNamesByPid(Binder.getCallingPid());
                for (PluginPackageParser pluginPackageParser : mPluginCache.values()) {
                    List<ProviderInfo> providerInfos = pluginPackageParser.getProviders();
                    for (ProviderInfo providerInfo : providerInfos) {
                        if (TextUtils.equals(providerInfo.authority, name) && pkgs.contains(providerInfo.packageName)) {
                            return providerInfo;
                        }
                    }
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @Override
    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) throws RemoteException {
        boolean success = false;
        try {
            if (TextUtils.isEmpty(packageName)) {
                return;
            }

            PluginPackageParser parser = mPluginCache.get(packageName);
            if (parser == null) {
                return;
            }
            ApplicationInfo applicationInfo = parser.getApplicationInfo(0);
            PluginFileHelper.deleteDir(new File(applicationInfo.dataDir, "caches").getName());
            success = true;
        } catch (Exception e) {
            handleException(e);
        } finally {
            if (observer != null) {
                observer.onRemoveCompleted(packageName, success);
            }
        }
    }

    @Override
    public void clearApplicationUserData(String packageName, IPackageDataObserver observer) throws RemoteException {
        boolean success = false;
        try {
            if (TextUtils.isEmpty(packageName)) {
                return;
            }

            PluginPackageParser parser = mPluginCache.get(packageName);
            if (parser == null) {
                return;
            }
            ApplicationInfo applicationInfo = parser.getApplicationInfo(0);
            PluginFileHelper.deleteDir(applicationInfo.dataDir);
            success = true;
        } catch (Exception e) {
            handleException(e);
        } finally {
            if (observer != null) {
                observer.onRemoveCompleted(packageName, success);
            }
        }
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws RemoteException {
        waitForReadyInner();
        try {
            if (TextUtils.equals(packageName, mContext.getPackageName())) {
                return null;
            }
            PluginPackageParser parser = mPluginCache.get(packageName);
            if (parser != null) {
                return parser.getApplicationInfo(flags);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return null;
    }


    @Override
    public int installPackage(String filepath, int flags) throws RemoteException {
        //install plugin
        String apkfile = null;
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filepath, 0);
            if (info == null) {
                return PluginCodeDefine.INSTALL_FAILED_INVALID_APK;
            }

            apkfile = PluginFileHelper.getPluginApkFile(mContext, info.packageName);

            if ((flags & PluginCodeDefine.INSTALL_UPDATE) != 0) {
                forceStopPackage(info.packageName);
                if (mPluginCache.containsKey(info.packageName)) {
                    deleteApplicationCacheFiles(info.packageName, null);
                }
                new File(apkfile).delete();
                PluginFileHelper.copyFile(filepath, apkfile);
                PluginPackageParser parser = new PluginPackageParser(mContext, new File(apkfile));
                parser.collectCertificates(0);
                PackageInfo pkgInfo = parser.getPackageInfo(PackageManager.GET_PERMISSIONS | PackageManager.GET_SIGNATURES);
                if (pkgInfo != null && pkgInfo.requestedPermissions != null && pkgInfo.requestedPermissions.length > 0) {
                    for (String requestedPermission : pkgInfo.requestedPermissions) {
                        boolean b = false;
                        try {
                            b = pm.getPermissionInfo(requestedPermission, 0) != null;
                        } catch (NameNotFoundException e) {
                        }
                        if (!mHostRequestedPermission.contains(requestedPermission) && b) {
                            LogUtil.i(TAG, "No Permission %s", requestedPermission);
                            new File(apkfile).delete();
                            return PluginCodeDefine.INSTALL_FAILED_PERMISSION_REQUESTED;
                        }
                    }
                }
                saveSignatures(pkgInfo);
                SoFileHelper.copyNativeLibs(mContext, apkfile, parser.getApplicationInfo(0));
                mPluginCache.put(parser.getPackageName(), parser);
                mActivityManagerService.onPkgInstalled(mPluginCache, parser, parser.getPackageName());
                sendInstalledBroadcast(info.packageName);
                return PluginCodeDefine.INSTALL_SUCCEEDED;
            } else {
                if (mPluginCache.containsKey(info.packageName)) {
                    return PluginCodeDefine.INSTALL_FAILED_ALREADY_EXISTS;
                } else {
                    forceStopPackage(info.packageName);
                    new File(apkfile).delete();
                    PluginFileHelper.copyFile(filepath, apkfile);
                    PluginPackageParser parser = new PluginPackageParser(mContext, new File(apkfile));
                    parser.collectCertificates(0);
                    PackageInfo pkgInfo = parser.getPackageInfo(PackageManager.GET_PERMISSIONS | PackageManager.GET_SIGNATURES);
                    if (pkgInfo != null && pkgInfo.requestedPermissions != null && pkgInfo.requestedPermissions.length > 0) {
                        for (String requestedPermission : pkgInfo.requestedPermissions) {
                            boolean b = false;
                            try {
                                b = pm.getPermissionInfo(requestedPermission, 0) != null;
                            } catch (NameNotFoundException e) {
                            }
                            if (!mHostRequestedPermission.contains(requestedPermission) && b) {
                                LogUtil.i(TAG, "No Permission %s", requestedPermission);
                                new File(apkfile).delete();
                                return PluginCodeDefine.INSTALL_FAILED_PERMISSION_REQUESTED;
                            }
                        }
                    }
                    saveSignatures(pkgInfo);
                    SoFileHelper.copyNativeLibs(mContext, apkfile, parser.getApplicationInfo(0));
                    mPluginCache.put(parser.getPackageName(), parser);
                    mActivityManagerService.onPkgInstalled(mPluginCache, parser, parser.getPackageName());
                    sendInstalledBroadcast(info.packageName);
                    return PluginCodeDefine.INSTALL_SUCCEEDED;
                }
            }
        } catch (Exception e) {
            if (apkfile != null) {
                new File(apkfile).delete();
            }
            handleException(e);
            return PluginCodeDefine.INSTALL_FAILED_INTERNAL_ERROR;
        }
    }


    private void sendInstalledBroadcast(String packageName) {
        Intent intent = new Intent(PluginManager.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package://" + packageName));
        mContext.sendBroadcast(intent);
    }

    private void sendUninstalledBroadcast(String packageName) {
        Intent intent = new Intent(PluginManager.ACTION_PACKAGE_REMOVED);
        intent.setData(Uri.parse("package://" + packageName));
        mContext.sendBroadcast(intent);
    }


    @Override
    public int deletePackage(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginCache.containsKey(packageName)) {
                forceStopPackage(packageName);

                PluginPackageParser parser;
                synchronized (mPluginCache) {
                    parser = mPluginCache.remove(packageName);
                }
                PluginFileHelper.deleteDir(PluginFileHelper.makePluginBaseDir(mContext, packageName));
                mActivityManagerService.onPkgDeleted(mPluginCache, parser, packageName);
                mSignatureCache.remove(packageName);
                sendUninstalledBroadcast(packageName);
                return PluginCodeDefine.DELETE_SUCCEEDED;
            }
        } catch (Exception e) {
            handleException(e);
        }
        return PluginCodeDefine.DELETE_FAILED_INTERNAL_ERROR;
    }

    @Override
    public List<ActivityInfo> getReceivers(String packageName, int flags) throws RemoteException {
        try {
            String pkg = getAndCheckCallingPkg(packageName);
            if (pkg != null) {
                PluginPackageParser parser = mPluginCache.get(packageName);
                if (parser != null) {
                    return new ArrayList<ActivityInfo>(parser.getReceivers());
                }
            }
        } catch (Exception e) {
            RemoteException remoteException = new RemoteException();
            remoteException.setStackTrace(e.getStackTrace());
            throw remoteException;
        }
        return new ArrayList<ActivityInfo>(0);
    }

    @Override
    public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) throws RemoteException {
        try {
            String pkg = getAndCheckCallingPkg(info.packageName);
            if (pkg != null) {
                PluginPackageParser parser = mPluginCache.get(info.packageName);
                if (parser != null) {
                    List<IntentFilter> filters = parser.getReceiverIntentFilter(info);
                    if (filters != null && filters.size() > 0) {
                        return new ArrayList<IntentFilter>(filters);
                    }
                }
            }
            return new ArrayList<IntentFilter>(0);
        } catch (Exception e) {
            RemoteException remoteException = new RemoteException();
            remoteException.setStackTrace(e.getStackTrace());
            throw remoteException;
        }
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) throws RemoteException {
        PackageManager pm = mContext.getPackageManager();
        Signature[] signatures1 = new Signature[0];
        try {
            signatures1 = getSignature(pkg1, pm);
        } catch (NameNotFoundException e) {
            return PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
        }
        Signature[] signatures2 = new Signature[0];
        try {
            signatures2 = getSignature(pkg2, pm);
        } catch (NameNotFoundException e) {
            return PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
        }


        boolean pkg1Signed = signatures1 != null && signatures1.length > 0;
        boolean pkg2Signed = signatures2 != null && signatures2.length > 0;

        if (!pkg1Signed && !pkg2Signed) {
            return PackageManager.SIGNATURE_NEITHER_SIGNED;
        } else if (!pkg1Signed && pkg2Signed) {
            return PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
        } else if (pkg1Signed && !pkg2Signed) {
            return PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
        } else {
            if (signatures1.length == signatures2.length) {
                for (int i = 0; i < signatures1.length; i++) {
                    Signature s1 = signatures1[i];
                    Signature s2 = signatures2[i];
                    if (!Arrays.equals(s1.toByteArray(), s2.toByteArray())) {
                        return PackageManager.SIGNATURE_NO_MATCH;
                    }
                }
                return PackageManager.SIGNATURE_MATCH;
            } else {
                return PackageManager.SIGNATURE_NO_MATCH;
            }
        }
    }

    private Signature[] getSignature(String pkg, PackageManager pm) throws RemoteException, NameNotFoundException {
        PackageInfo info = getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
        if (info == null) {
            info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
        }
        if (info == null) {
            throw new NameNotFoundException();
        }
        return info.signatures;
    }

    private void saveSignatures(PackageInfo pkgInfo) {
        if (pkgInfo != null && pkgInfo.signatures != null) {
            int i = 0;
            for (Signature signature : pkgInfo.signatures) {
                File file = new File(PluginFileHelper.getPluginSignatureFile(mContext, pkgInfo.packageName, i));
                try {
                    PluginFileHelper.writeToFile(file, signature.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                    file.delete();
                    PluginFileHelper.deleteDir(PluginFileHelper.getPluginSignatureDir(mContext, pkgInfo.packageName));
                    break;
                }
                i++;
            }
        }
    }

    private Signature[] readSignatures(String packageName) {
        List<String> fils = PluginFileHelper.getPluginSignatureFiles(mContext, packageName);
        List<Signature> signatures = new ArrayList<Signature>(fils.size());
        int i = 0;
        for (String file : fils) {
            try {
                byte[] data = PluginFileHelper.readFromFile(new File(file));
                if (data != null) {
                    Signature sin = new Signature(data);
                    signatures.add(sin);
                } else {
                    return null;
                }
                i++;
            } catch (Exception e) {
                return null;
            }
        }
        return signatures.toArray(new Signature[signatures.size()]);
    }

    //////////////////////////////////////
    //
    //  THIS API FOR ACTIVITY MANAGER
    //
    //////////////////////////////////////

    @Override
    public ActivityInfo selectStubActivityInfo(ActivityInfo pluginInfo) throws RemoteException {
        return mActivityManagerService.selectStubActivityInfo(Binder.getCallingPid(), Binder.getCallingUid(), pluginInfo);
    }

    @Override
    public ActivityInfo selectStubActivityInfoByIntent(Intent intent) throws RemoteException {
        ActivityInfo ai = null;
        if (intent.getComponent() != null) {
            ai = getActivityInfo(intent.getComponent(), 0);
        } else {
            ResolveInfo resolveInfo = resolveIntent(intent, intent.resolveTypeIfNeeded(mContext.getContentResolver()), 0);
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                ai = resolveInfo.activityInfo;
            }
        }

        if (ai != null) {
            return selectStubActivityInfo(ai);
        }
        return null;
    }

    @Override
    public ServiceInfo selectStubServiceInfo(ServiceInfo targetInfo) throws RemoteException {
        return mActivityManagerService.selectStubServiceInfo(Binder.getCallingPid(), Binder.getCallingUid(), targetInfo);
    }

    @Override
    public ServiceInfo selectStubServiceInfoByIntent(Intent intent) throws RemoteException {
        ServiceInfo ai = null;
        if (intent.getComponent() != null) {
            ai = getServiceInfo(intent.getComponent(), 0);
        } else {
            ResolveInfo resolveInfo = resolveIntent(intent, intent.resolveTypeIfNeeded(mContext.getContentResolver()), 0);
            if (resolveInfo.serviceInfo != null) {
                ai = resolveInfo.serviceInfo;
            }
        }

        if (ai != null) {
            return selectStubServiceInfo(ai);
        }
        return null;
    }


    @Override
    public ServiceInfo getTargetServiceInfo(ServiceInfo targetInfo) throws RemoteException {
        return mActivityManagerService.getTargetServiceInfo(Binder.getCallingPid(), Binder.getCallingUid(), targetInfo);
    }

    @Override
    public ProviderInfo selectStubProviderInfo(String name) throws RemoteException {
        ProviderInfo targetInfo = resolveContentProvider(name, 0);
        return mActivityManagerService.selectStubProviderInfo(Binder.getCallingPid(), Binder.getCallingUid(), targetInfo);
    }

    @Override
    public List<String> getPackageNameByPid(int pid) throws RemoteException {
        List<String> packageNameByProcessName = mActivityManagerService.getPackageNamesByPid(pid);
        if (packageNameByProcessName != null) {
            return new ArrayList<String>(packageNameByProcessName);
        } else {
            return null;
        }
    }

    @Override
    public String getProcessNameByPid(int pid) throws RemoteException {
        return mActivityManagerService.getProcessNameByPid(pid);
    }


    @Override
    public boolean killBackgroundProcesses(String pluginPackageName) throws RemoteException {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        boolean success = false;
        for (RunningAppProcessInfo info : infos) {
            if (info.pkgList != null) {
                String[] pkgListCopy = Arrays.copyOf(info.pkgList, info.pkgList.length);
                Arrays.sort(pkgListCopy);
                if (Arrays.binarySearch(pkgListCopy, pluginPackageName) >= 0 && info.pid != android.os.Process.myPid()) {
                    LogUtil.i(TAG, "killBackgroundProcesses(%s),pkgList=%s,pid=%s", pluginPackageName, Arrays.toString(info.pkgList), info.pid);
                    android.os.Process.killProcess(info.pid);
                    success = true;
                }
            }
        }
        return success;
    }

    @Override
    public boolean killApplicationProcess(String pluginPackageName) throws RemoteException {
        return killBackgroundProcesses(pluginPackageName);
    }

    @Override
    public boolean forceStopPackage(String pluginPackageName) throws RemoteException {
        return killBackgroundProcesses(pluginPackageName);
    }

    @Override
    public boolean registerApplicationCallback(IApplicationCallback callback) throws RemoteException {
        return mActivityManagerService.registerApplicationCallback(Binder.getCallingPid(), Binder.getCallingUid(), callback);
    }

    @Override
    public boolean unregisterApplicationCallback(IApplicationCallback callback) throws RemoteException {
        return mActivityManagerService.unregisterApplicationCallback(Binder.getCallingPid(), Binder.getCallingUid(), callback);
    }

    @Override
    public void onActivityCreated(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {
        mActivityManagerService.onActivityCreated(Binder.getCallingPid(), Binder.getCallingUid(), stubInfo, targetInfo);
    }

    @Override
    public void onActivityDestory(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {
        mActivityManagerService.onActivityDestory(Binder.getCallingPid(), Binder.getCallingUid(), stubInfo, targetInfo);
    }

    @Override
    public void onServiceCreated(ServiceInfo stubInfo, ServiceInfo targetInfo) throws RemoteException {
        mActivityManagerService.onServiceCreated(Binder.getCallingPid(), Binder.getCallingUid(), stubInfo, targetInfo);
    }

    @Override
    public void onServiceDestory(ServiceInfo stubInfo, ServiceInfo targetInfo) throws RemoteException {
        mActivityManagerService.onServiceDestory(Binder.getCallingPid(), Binder.getCallingUid(), stubInfo, targetInfo);
    }

    @Override
    public void onProviderCreated(ProviderInfo stubInfo, ProviderInfo targetInfo) throws RemoteException {
        mActivityManagerService.onProviderCreated(Binder.getCallingPid(), Binder.getCallingUid(), stubInfo, targetInfo);
    }

    @Override
    public void reportMyProcessName(String stubProcessName, String targetProcessName, String targetPkg) throws RemoteException {
        mActivityManagerService.onReportMyProcessName(Binder.getCallingPid(), Binder.getCallingUid(), stubProcessName, targetProcessName, targetPkg);
    }

    public void onDestroy() {
        mActivityManagerService.onDestory();
    }

    @Override
    public void onActivtyOnNewIntent(ActivityInfo stubInfo, ActivityInfo targetInfo, Intent intent) throws RemoteException {
        mActivityManagerService.onActivtyOnNewIntent(Binder.getCallingPid(), Binder.getCallingUid(), stubInfo, targetInfo, intent);
    }

    @Override
    public int getMyPid() {
        return android.os.Process.myPid();
    }

}
