package com.season.plugin.hookcore.handle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import com.season.lib.util.LogUtil;
import com.season.plugin.hookcore.Env;
import com.season.plugin.core.PluginManager;
import com.season.plugin.core.PluginProcessManager;
import com.season.plugin.hookcore.ProxyHookPackageManager;
import com.season.plugin.stub.util.RunningActivities;
import com.season.lib.support.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


/**
 * Disc: 处理activity生命周期
 * Hook点：
 * @see com.season.plugin.hookcore.HookInstrumentation
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class PluginInstrumentation extends Instrumentation {

    protected static final String TAG = PluginInstrumentation.class.getSimpleName();

    protected Instrumentation mTarget;
    private final Context mHostContext;
    private boolean enable = true;

    public void setEnable(boolean enable) {

        this.enable = enable;
        this.enable = true;
    }

    public PluginInstrumentation(Context hostContext, Instrumentation target) {
        mTarget = target;
        mHostContext = hostContext;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className,
                                Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        ComponentName targetComponentName = intent.resolveActivity(mHostContext.getPackageManager());
        try {
            ClassLoader pluginClassLoader = PluginProcessManager.getPluginClassLoader(targetComponentName.getPackageName());
            LogUtil.e("1212>>",  pluginClassLoader.toString());
            LogUtil.e("1212",  cl.toString());
            //return mTarget.newActivity(pluginClassLoader, className, intent);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return mTarget.newActivity(cl, className, intent);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        try {
            ClassLoader pluginClassLoader = PluginProcessManager.getPluginClassLoader(context.getPackageName());
            //return mTarget.newApplication(pluginClassLoader, className, context);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return mTarget.newApplication(cl, className, context);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if (enable) {
            ProxyHookPackageManager.fixContextPackageManager(activity);
            try {
                PluginProcessManager.fakeSystemService(mHostContext, activity);
            } catch (Exception e) {
                LogUtil.e(TAG, "callActivityOnCreate:fakeSystemService", e);
            }
            try {
                onActivityCreated(activity);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "callActivityOnCreate:onActivityCreated", e);
            }

            try {
                fixBaseContextImplOpsPackage(activity.getBaseContext());
            } catch (Exception e) {
                LogUtil.e(TAG, "callActivityOnCreate:fixBaseContextImplOpsPackage", e);
            }

            try {
                fixBaseContextImplContentResolverOpsPackage(activity.getBaseContext());
            } catch (Exception e) {
                LogUtil.e(TAG, "callActivityOnCreate:fixBaseContextImplContentResolverOpsPackage", e);
            }


        }


        if (mTarget != null) {
            mTarget.callActivityOnCreate(activity, icicle);
        } else {
            super.callActivityOnCreate(activity, icicle);
        }
    }

    private void fixBaseContextImplOpsPackage(Context context) throws IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && context != null && !TextUtils.equals(context.getPackageName(), mHostContext.getPackageName())) {
            Context baseContext = context;
            Class clazz = baseContext.getClass();
            Field mOpPackageName = FieldUtils.getDeclaredField(clazz, "mOpPackageName", true);
            if (mOpPackageName != null) {
                Object valueObj = mOpPackageName.get(baseContext);
                if (valueObj instanceof String) {
                    String opPackageName = ((String) valueObj);
                    if (!TextUtils.equals(opPackageName, mHostContext.getPackageName())) {
                        mOpPackageName.set(baseContext, mHostContext.getPackageName());
                        LogUtil.i(TAG, "fixBaseContextImplOpsPackage OK!Context=%s,", baseContext);
                    }
                }
            }
        }
    }

    private void fixBaseContextImplContentResolverOpsPackage(Context context) throws IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && context != null && !TextUtils.equals(context.getPackageName(), mHostContext.getPackageName())) {
            Context baseContext = context;
            Class clazz = baseContext.getClass();
            Field mContentResolver = FieldUtils.getDeclaredField(clazz, "mContentResolver", true);
            if (mContentResolver != null) {
                Object valueObj = mContentResolver.get(baseContext);
                if (valueObj instanceof ContentResolver) {
                    ContentResolver contentResolver = ((ContentResolver) valueObj);
                    Field mPackageName = FieldUtils.getDeclaredField(ContentResolver.class, "mPackageName", true);
                    Object mPackageNameValueObj = mPackageName.get(contentResolver);
                    if (mPackageNameValueObj != null && mPackageNameValueObj instanceof String) {
                        String packageName = ((String) mPackageNameValueObj);
                        if (!TextUtils.equals(packageName, mHostContext.getPackageName())) {
                            mPackageName.set(contentResolver, mHostContext.getPackageName());
                            LogUtil.i(TAG, "fixBaseContextImplContentResolverOpsPackage OK!Context=%s,contentResolver=%s", baseContext, contentResolver);
                        }
                    }

                }
            }
        }
    }


    private void onActivityCreated(Activity activity) throws RemoteException {
        try {
            Intent targetIntent = activity.getIntent();
            if (targetIntent != null) {
                ActivityInfo targetInfo = targetIntent.getParcelableExtra(Env.EXTRA_TARGET_INFO);
                ActivityInfo stubInfo = targetIntent.getParcelableExtra(Env.EXTRA_STUB_INFO);
                if (targetInfo != null && stubInfo != null) {
                    RunningActivities.onActivtyCreate(activity, targetInfo, stubInfo);
                    activity.setRequestedOrientation(targetInfo.screenOrientation);
                    PluginManager.getInstance().onActivityCreated(stubInfo, targetInfo);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fixTaskDescription(activity, targetInfo);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "onActivityCreated fail", e);
        }
    }

    private void onActivityOnNewIntent(Activity activity, Intent intent) throws RemoteException {
        //
        try {
            Intent targetIntent = activity.getIntent();
            if (targetIntent != null) {
                ActivityInfo targetInfo = targetIntent.getParcelableExtra(Env.EXTRA_TARGET_INFO);
                ActivityInfo stubInfo = targetIntent.getParcelableExtra(Env.EXTRA_STUB_INFO);
                if (targetInfo != null && stubInfo != null) {
                    RunningActivities.onActivtyOnNewIntent(activity, targetInfo, stubInfo, intent);
                    PluginManager.getInstance().onActivtyOnNewIntent(stubInfo, targetInfo, intent);
                }
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "onActivityCreated fail", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void fixTaskDescription(Activity activity, ActivityInfo targetInfo) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PackageManager pm = mHostContext.getPackageManager();
                String lablel = String.valueOf(targetInfo.loadLabel(pm));
                Drawable icon = targetInfo.loadIcon(pm);
                Bitmap bitmap = null;
                if (icon instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable) icon).getBitmap();
                }
                if (bitmap != null) {
                    activity.setTaskDescription(new android.app.ActivityManager.TaskDescription(lablel, bitmap));
                } else {
                    activity.setTaskDescription(new android.app.ActivityManager.TaskDescription(lablel));
                }
            }
        } catch (Throwable e) {
            LogUtil.w(TAG, "fixTaskDescription fail", e);
        }
    }

    private void onActivityDestory(Activity activity) throws RemoteException {
        Intent targetIntent = activity.getIntent();
        if (targetIntent != null) {
            ActivityInfo targetInfo = targetIntent.getParcelableExtra(Env.EXTRA_TARGET_INFO);
            ActivityInfo stubInfo = targetIntent.getParcelableExtra(Env.EXTRA_STUB_INFO);
            if (targetInfo != null && stubInfo != null) {
                PluginManager.getInstance().onActivityDestory(stubInfo, targetInfo);
            }
        }
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        if (mTarget != null) {
            mTarget.callActivityOnDestroy(activity);
        } else {
            super.callActivityOnDestroy(activity);
        }
        RunningActivities.onActivtyDestory(activity);

        if (enable) {
            try {
                onActivityDestory(activity);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "callActivityOnDestroy:onActivityDestory", e);
            }
        }
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        if (enable) {
            ProxyHookPackageManager.fixContextPackageManager(app);
            try {
                PluginProcessManager.fakeSystemService(mHostContext, app);
            } catch (Exception e) {
                LogUtil.e(TAG, "fakeSystemService", e);
            }

            try {
                fixBaseContextImplOpsPackage(app.getBaseContext());
            } catch (Exception e) {
                LogUtil.e(TAG, "callApplicationOnCreate:fixBaseContextImplOpsPackage", e);
            }

            try {
                fixBaseContextImplContentResolverOpsPackage(app.getBaseContext());
            } catch (Exception e) {
                LogUtil.e(TAG, "callActivityOnCreate:fixBaseContextImplContentResolverOpsPackage", e);
            }
        }

        if (mTarget != null) {
            mTarget.callApplicationOnCreate(app);
        } else {
            super.callApplicationOnCreate(app);
        }

        if (enable) {
            try {
                PluginProcessManager.registerStaticReceiver(app, app.getApplicationInfo(), app.getClassLoader());
            } catch (Exception e) {
                LogUtil.e(TAG, "registerStaticReceiver", e);
            }
        }
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
//        if (activity != null && intent != null) {
//            intent.setClassName(activity.getPackageName(), activity.getClass().getName());
//        }
        try {
            Intent newIntent = intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
            if (newIntent != null) {
                intent = newIntent;
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, "callActivityOnNewIntent:read EXTRA_TARGET_INTENT", e);
        }
        if (enable) {
            try {
                onActivityOnNewIntent(activity, intent);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "callActivityOnNewIntent:onActivityOnNewIntent", e);
            }
        }
        if (mTarget != null) {
            mTarget.callActivityOnNewIntent(activity, intent);
        } else {
            super.callActivityOnNewIntent(activity, intent);
        }
    }


}
