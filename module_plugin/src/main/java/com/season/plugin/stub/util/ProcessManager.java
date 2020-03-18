package com.season.plugin.stub.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;


import com.season.lib.util.LogUtil;
import com.season.plugin.parser.PluginPackageParser;
import com.season.plugin.stub.AbstractServiceStub;
import com.season.pluginlib.IApplicationCallback;
import com.season.plugin.core.IPluginManagerImpl;
import com.season.lib.reflect.FieldUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 这是一个比较复杂的进程管理服务。
 * 主要实现的功能为：
 * 1、系统预定义N个进程。每个进程下有4中launch mode的activity，1个服务，一个ContentProvider。
 * 2、每个插件可以在多个进程中运行，这由插件自己的processName属性决定。
 * 3、插件系统最多可以同时运行N个进程，M个插件(M <= N or M >= N)。
 * 4、多个插件运行在同一个进程中，如果他们的签名相同。（我们可以通过一个开关来决定。）
 * 5、在运行第M+1个插件时，如果预定义的N个进程被占满，最低优先级的进程会被kill掉。腾出预定义的进程用来运行此个插件。
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/10.
 */
public class ProcessManager {

    private static final String TAG = ProcessManager.class.getSimpleName();
    private StaticProcessList mStaticProcessList = new StaticProcessList();
    private RunningProcessList mRunningProcessList = new RunningProcessList();

    protected Context mHostContext;
    public ProcessManager(Context hostContext) {
        mHostContext = hostContext;
        mRunningProcessList.setContext(mHostContext);
    }

    private RemoteCallbackList<IApplicationCallback> mRemoteCallbackList;
    public void onCreate(IPluginManagerImpl iPluginManager) throws Exception {
        if (mRemoteCallbackList == null) {
            mRemoteCallbackList = new MyRemoteCallbackList();
        }
        AttributeCache.init(mHostContext);
        mStaticProcessList.onCreate(mHostContext);
        mRunningProcessList.setContext(mHostContext);
    }


    private static class ProcessCookie {
        private ProcessCookie(int pid, int uid) {
            this.pid = pid;
            this.uid = uid;
        }

        private final int pid;
        private final int uid;
    }
    private class MyRemoteCallbackList extends RemoteCallbackList<IApplicationCallback> {
        @Override
        public void onCallbackDied(IApplicationCallback callback, Object cookie) {
            super.onCallbackDied(callback, cookie);
            if (cookie != null && cookie instanceof ProcessCookie) {
                ProcessCookie p = (ProcessCookie) cookie;
                onProcessDied(p.pid, p.uid);
            }
        }
    }


    public void onDestory() {
        mRunningProcessList.clear();
        mStaticProcessList.clear();
        runProcessGC();
        mRemoteCallbackList.kill();
        mRemoteCallbackList = null;
    }

    protected void onProcessDied(int pid, int uid) {
        mRunningProcessList.onProcessDied(pid, uid);
        runProcessGC();
    }

    private String getProcessName(Context context, int pid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> raps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo rap : raps) {
            if (rap != null && rap.pid == pid) {
                return rap.processName;
            }
        }
        return null;
    }

    public boolean registerApplicationCallback(int callingPid, int callingUid, IApplicationCallback callback) {
        mRunningProcessList.addItem(callingPid, callingUid);
        if (callingPid == android.os.Process.myPid()) {
            String stubProcessName = getProcessName(mHostContext, callingPid);
            String targetProcessName = getProcessName(mHostContext, callingPid);
            String targetPkg = mHostContext.getPackageName();
            mRunningProcessList.setProcessName(callingPid, stubProcessName, targetProcessName, targetPkg);
        }
        if (TextUtils.equals(mHostContext.getPackageName(), getProcessName(mHostContext, callingPid))) {
            String stubProcessName = mHostContext.getPackageName();
            String targetProcessName = mHostContext.getPackageName();
            String targetPkg = mHostContext.getPackageName();
            mRunningProcessList.setProcessName(callingPid, stubProcessName, targetProcessName, targetPkg);
        }
        return mRemoteCallbackList.register(callback, new ProcessCookie(callingPid, callingUid));
    }

    public boolean unregisterApplicationCallback(int callingPid, int callingUid, IApplicationCallback callback) {
        return mRemoteCallbackList.unregister(callback);
    }

    public void onPkgDeleted(Map<String, PluginPackageParser> pluginCache, PluginPackageParser parser, String packageName) throws Exception {
    }

    public void onPkgInstalled(Map<String, PluginPackageParser> pluginCache, PluginPackageParser parser, String packageName) throws Exception {
    }

    public ProviderInfo selectStubProviderInfo(int callingPid, int callingUid, ProviderInfo targetInfo) throws RemoteException {
        runProcessGC();

        //先从正在运行的进程中查找看是否有符合条件的进程，如果有则直接使用之
        String stubProcessName1 = mRunningProcessList.getStubProcessByTarget(targetInfo);
        if (stubProcessName1 != null) {
            List<ProviderInfo> stubInfos = mStaticProcessList.getProviderInfoForProcessName(stubProcessName1);
            for (ProviderInfo stubInfo : stubInfos) {
                if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                    mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                    return stubInfo;
                }
            }
        }

        List<String> stubProcessNames = mStaticProcessList.getProcessNames();
        for (String stubProcessName : stubProcessNames) {
            List<ProviderInfo> stubInfos = mStaticProcessList.getProviderInfoForProcessName(stubProcessName);
            if (mRunningProcessList.isProcessRunning(stubProcessName)) {
                if (mRunningProcessList.isPkgEmpty(stubProcessName)) {//空进程，没有运行任何插件包。
                    for (ProviderInfo stubInfo : stubInfos) {
                        if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        }
                    }
                    throw throwException("没有找到合适的StubInfo");
                } else if (mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName, stubProcessName, targetInfo.processName)) {
                    for (ProviderInfo stubInfo : stubInfos) {
                        if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        }
                    }
                    throw throwException("没有找到合适的StubInfo");
                } else {
                    //需要处理签名一样的情况。
                }
            } else {
                for (ProviderInfo stubInfo : stubInfos) {
                    if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                        mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                        return stubInfo;
                    }
                }
                throw throwException("没有找到合适的StubInfo");
            }
        }
        throw throwException("没有可用的进程了");
    }


    public ServiceInfo getTargetServiceInfo(int callingPid, int callingUid, ServiceInfo stubInfo) throws RemoteException {
        return null;
    }


    public String getProcessNameByPid(int pid) {
        return mRunningProcessList.getTargetProcessNameByPid(pid);
    }


    public ServiceInfo selectStubServiceInfo(int callingPid, int callingUid, ServiceInfo targetInfo) throws RemoteException {
        runProcessGC();

        //先从正在运行的进程中查找看是否有符合条件的进程，如果有则直接使用之
        String stubProcessName1 = mRunningProcessList.getStubProcessByTarget(targetInfo);
        if (stubProcessName1 != null) {
            List<ServiceInfo> stubInfos = mStaticProcessList.getServiceInfoForProcessName(stubProcessName1);
            for (ServiceInfo stubInfo : stubInfos) {
                if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                    mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                    return stubInfo;
                }
            }
        }

        List<String> stubProcessNames = mStaticProcessList.getProcessNames();
        for (String stubProcessName : stubProcessNames) {
            List<ServiceInfo> stubInfos = mStaticProcessList.getServiceInfoForProcessName(stubProcessName);
            if (mRunningProcessList.isProcessRunning(stubProcessName)) {//该预定义的进程正在运行。
                if (mRunningProcessList.isPkgEmpty(stubProcessName)) {//空进程，没有运行任何插件包。
                    for (ServiceInfo stubInfo : stubInfos) {
                        if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        }
                    }
                    throw throwException("没有找到合适的StubInfo");
                } else if (mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName, stubProcessName, targetInfo.processName)) {
                    for (ServiceInfo stubInfo : stubInfos) {
                        if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        }
                    }
                    throw throwException("没有找到合适的StubInfo");
                } else {
                    //这里需要考虑签名一样的情况，多个插件公用一个进程。
                }
            } else { //该预定义的进程没有。
                for (ServiceInfo stubInfo : stubInfos) {
                    if (!mRunningProcessList.isStubInfoUsed(stubInfo)) {
                        mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                        return stubInfo;
                    }
                }
                throw throwException("没有找到合适的StubInfo");
            }
        }
        throw throwException("没有可用的进程了");
    }

    private RemoteException throwException(String msg) {
        RemoteException remoteException = new RemoteException();
        remoteException.initCause(new RuntimeException(msg));
        return remoteException;
    }


    public void onActivityCreated(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo) {
        mRunningProcessList.addActivityInfo(callingPid, callingUid, stubInfo, targetInfo);
    }

    public void onActivityDestory(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo) {
        mRunningProcessList.removeActivityInfo(callingPid, callingUid, stubInfo, targetInfo);
        runProcessGC();
    }

    public void onActivtyOnNewIntent(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo, Intent intent) {
        mRunningProcessList.addActivityInfo(callingPid, callingUid, stubInfo, targetInfo);
    }

    public void onServiceCreated(int callingPid, int callingUid, ServiceInfo stubInfo, ServiceInfo targetInfo) {
        mRunningProcessList.addServiceInfo(callingPid, callingUid, stubInfo, targetInfo);
    }

    public void onServiceDestory(int callingPid, int callingUid, ServiceInfo stubInfo, ServiceInfo targetInfo) {
        mRunningProcessList.removeServiceInfo(callingPid, callingUid, stubInfo, targetInfo);
        runProcessGC();
    }

    public void onProviderCreated(int callingPid, int callingUid, ProviderInfo stubInfo, ProviderInfo targetInfo) {
        mRunningProcessList.addProviderInfo(callingPid, callingUid, stubInfo, targetInfo);
    }

    public void onReportMyProcessName(int callingPid, int callingUid, String stubProcessName, String targetProcessName, String targetPkg) {
        mRunningProcessList.setProcessName(callingPid, stubProcessName, targetProcessName, targetPkg);
    }

    public List<String> getPackageNamesByPid(int pid) {
        return new ArrayList<String>(mRunningProcessList.getPackageNameByPid(pid));
    }

    public ActivityInfo selectStubActivityInfo(int callingPid, int callingUid, ActivityInfo targetInfo) throws RemoteException {
        runProcessGC();
        boolean Window_windowIsTranslucent = false;
        boolean Window_windowIsFloating = false;
        boolean Window_windowShowWallpaper = false;
        try {
            Class<?> R_Styleable_Class = Class.forName("com.android.internal.R$styleable");
            int[] R_Styleable_Window = (int[]) FieldUtils.readStaticField(R_Styleable_Class, "Window");
            int R_Styleable_Window_windowIsTranslucent = (int) FieldUtils.readStaticField(R_Styleable_Class, "Window_windowIsTranslucent");
            int R_Styleable_Window_windowIsFloating = (int) FieldUtils.readStaticField(R_Styleable_Class, "Window_windowIsFloating");
            int R_Styleable_Window_windowShowWallpaper = (int) FieldUtils.readStaticField(R_Styleable_Class, "Window_windowShowWallpaper");

            AttributeCache.Entry ent = AttributeCache.instance().get(targetInfo.packageName, targetInfo.theme,
                    R_Styleable_Window);
            if (ent != null && ent.array != null) {
                Window_windowIsTranslucent = ent.array.getBoolean(R_Styleable_Window_windowIsTranslucent, false);
                Window_windowIsFloating = ent.array.getBoolean(R_Styleable_Window_windowIsFloating, false);
                Window_windowShowWallpaper = ent.array.getBoolean(R_Styleable_Window_windowShowWallpaper, false);
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, "error on read com.android.internal.R$styleable", e);
        }

        boolean useDialogStyle = Window_windowIsTranslucent || Window_windowIsFloating || Window_windowShowWallpaper;

        //先从正在运行的进程中查找看是否有符合条件的进程，如果有则直接使用之
        String stubProcessName1 = mRunningProcessList.getStubProcessByTarget(targetInfo);
        if (stubProcessName1 != null) {
            List<ActivityInfo> stubInfos = mStaticProcessList.getActivityInfoForProcessName(stubProcessName1, useDialogStyle);
            for (ActivityInfo stubInfo : stubInfos) {
                if (stubInfo.launchMode == targetInfo.launchMode) {
                    if (stubInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
                        mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                        return stubInfo;
                    } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                        mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                        return stubInfo;
                    }
                }
            }
        }

        List<String> stubProcessNames = mStaticProcessList.getProcessNames();
        for (String stubProcessName : stubProcessNames) {
            List<ActivityInfo> stubInfos = mStaticProcessList.getActivityInfoForProcessName(stubProcessName, useDialogStyle);
            if (mRunningProcessList.isProcessRunning(stubProcessName)) {//该预定义的进程正在运行。
                if (mRunningProcessList.isPkgEmpty(stubProcessName)) {//空进程，没有运行任何插件包。
                    for (ActivityInfo stubInfo : stubInfos) {
                        if (stubInfo.launchMode == targetInfo.launchMode) {
                            if (stubInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            }
                        }
                    }
                    throw throwException("没有找到合适的StubInfo");
                } else if (mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName, stubProcessName, targetInfo.processName)) {
                    for (ActivityInfo stubInfo : stubInfos) {
                        if (stubInfo.launchMode == targetInfo.launchMode) {
                            if (stubInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            }
                        }
                    }
                    throw throwException("没有找到合适的StubInfo");
                } else {
                    //这里需要考虑签名一样的情况，多个插件公用一个进程。
                }
            } else { //该预定义的进程没有。
                for (ActivityInfo stubInfo : stubInfos) {
                    if (stubInfo.launchMode == targetInfo.launchMode) {
                        if (stubInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        }
                    }
                }
                throw throwException("没有找到合适的StubInfo");
            }
        }
        throw throwException("没有可用的进程了");
    }

    private static final Comparator<RunningAppProcessInfo> sProcessComparator = new Comparator<RunningAppProcessInfo>() {
        @Override
        public int compare(RunningAppProcessInfo lhs, RunningAppProcessInfo rhs) {
            if (lhs.importance == rhs.importance) {
                return 0;
            } else if (lhs.importance > rhs.importance) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    //运行进程GC
    private void runProcessGC() {
        if (mHostContext == null) {
            return;
        }
        ActivityManager am = (ActivityManager) mHostContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return;
        }

        List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        List<RunningAppProcessInfo> myInfos = new ArrayList<RunningAppProcessInfo>();
        if (infos == null || infos.size() < 0) {
            return;
        }

        List<String> pns = mStaticProcessList.getOtherProcessNames();
        pns.add(mHostContext.getPackageName());
        for (RunningAppProcessInfo info : infos) {
            if (info.uid == android.os.Process.myUid()
                    && info.pid != android.os.Process.myPid()
                    && !pns.contains(info.processName)
                    && mRunningProcessList.isPlugin(info.pid)
                    && !mRunningProcessList.isPersistentApplication(info.pid)
                    /*&& !mRunningProcessList.isPersistentApplication(infoView.pid)*/) {
                myInfos.add(info);
            }
        }
        Collections.sort(myInfos, sProcessComparator);
        for (RunningAppProcessInfo myInfo : myInfos) {
            if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_GONE) {
                doGc(myInfo);
            } else if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_EMPTY) {
                doGc(myInfo);
            } else if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                doGc(myInfo);
            } else if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                doGc(myInfo);
            } /*else if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE) {
                //杀死进程，不能保存状态。但是关我什么事？
            }*/ else if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
                //杀死进程
            } else if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                //看得见
            } else if (myInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                //前台进程。
            }
        }

    }

    private void doGc(RunningAppProcessInfo myInfo) {
        int activityCount = mRunningProcessList.getActivityCountByPid(myInfo.pid);
        int serviceCount = mRunningProcessList.getServiceCountByPid(myInfo.pid);
        int providerCount = mRunningProcessList.getProviderCountByPid(myInfo.pid);
        if (activityCount <= 0 && serviceCount <= 0 && providerCount <= 0) {
            //杀死空进程。
            LogUtil.i(TAG, "doGc kill process(pid=%s,uid=%s processName=%s)", myInfo.pid, myInfo.uid, myInfo.processName);
            try {
                android.os.Process.killProcess(myInfo.pid);
            } catch (Throwable e) {
                LogUtil.e(TAG, "error on killProcess", e);
            }
        } else if (activityCount <= 0 && serviceCount > 0 /*&& !mRunningProcessList.isPersistentApplication(myInfo.pid)*/) {
            List<String> names = mRunningProcessList.getStubServiceByPid(myInfo.pid);
            if (names != null && names.size() > 0) {
                for (String name : names) {
                    Intent service = new Intent();
                    service.setClassName(mHostContext.getPackageName(), name);
                    AbstractServiceStub.startKillService(mHostContext, service);
                    LogUtil.i(TAG, "doGc kill process(pid=%s,uid=%s processName=%s) service=%s", myInfo.pid, myInfo.uid, myInfo.processName, service);
                }
            }
        }
    }
}
