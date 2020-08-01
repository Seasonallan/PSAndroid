package com.season.plugin.hookcore;

import android.content.Context;
import android.os.Build;
import android.util.AndroidRuntimeException;

import com.season.lib.support.reflect.FieldUtils;
import com.season.lib.support.reflect.MethodUtils;
import com.season.lib.support.reflect.Utils;
import com.season.lib.util.LogUtil;
import com.season.plugin.compat.ActivityManagerCompat;
import com.season.plugin.compat.ActivityManagerNativeCompat;
import com.season.plugin.hookcore.handle.BaseHookHandle;
import com.season.plugin.hookcore.handle.HookHandleActivityManager;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

/**
 * Disc: hook ActivityManager拦截startActivity类似请求，替换intent
 * 绑定的代理
 * @see HookHandleActivityManager
 *  2020-03-17 10:07 更新：新增对23以上SDK的支持，26，27，28的ams获取方式是通过ActivityManager.getService()
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class ProxyHookActivityTaskManager extends BaseHookProxy {

    private static final String TAG = ProxyHookActivityTaskManager.class.getSimpleName();

    public ProxyHookActivityTaskManager(Context hostContext) {
        super(hostContext);
    }

    @Override
    public BaseHookHandle createHookHandle() {
        return new HookHandleActivityManager(mHostContext);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            LogUtil.i(TAG, ">>invoke>>>"+ method.getName());
            return super.invoke(proxy, method, args);
        } catch (SecurityException e) {
            String msg = String.format("msg[%s],args[%s]", e.getMessage(), Arrays.toString(args));
            SecurityException e1 = new SecurityException(msg);
            e1.initCause(e);
            throw e1;
        }
    }

    @Override
    public void onInstall() throws Throwable {
        Class cls = Class.forName("android.app.ActivityTaskManager");
        Object obj = FieldUtils.readStaticField(cls, "IActivityTaskManagerSingleton");
        if (obj == null) {
            MethodUtils.invokeStaticMethod(cls, "IActivityTaskManagerSingleton");
            obj = FieldUtils.readStaticField(cls, "IActivityTaskManagerSingleton");
        }
        Object obj1 = FieldUtils.readField(obj, "mInstance");
        if (obj1 == null) {
            MethodUtils.invokeMethod(obj, "get");
            obj1 = FieldUtils.readField(obj, "mInstance");
        }
        setOldObj(obj1);
        List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
        Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
        final Object object = Proxy.newProxyInstance(mOldObj.getClass().getClassLoader(), ifs,
                this);

        //这里先写一次，防止后面找不到Singleton类导致的挂钩子失败的问题。
        FieldUtils.writeField(obj, "mInstance", object);

        LogUtil.i(TAG, "28 Install ActivityTaskManager 2 BaseHook  old=%s,new=%s", mOldObj, object);
        //这里使用方式1，如果成功的话，会导致上面的写操作被覆盖。
        FieldUtils.writeStaticField(cls, "IActivityTaskManagerSingleton", new android.util.Singleton<Object>() {
            @Override
            protected Object create() {
                LogUtil.i(TAG, "28 Install ActivityTaskManager 3 BaseHook  old=%s,new=%s", mOldObj, object);
                return object;
            }
        });
    }


}
