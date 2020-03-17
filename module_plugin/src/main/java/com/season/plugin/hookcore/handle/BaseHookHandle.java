package com.season.plugin.hookcore.handle;

import android.content.Context;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Disc: 动态代理时需要invoke的方法基类
 * 实现的实例
 * @see HookHandleActivityManager 动态代理ActivityManager
 * @see HookHandlePackageManager 动态代理PackageManager
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public abstract class BaseHookHandle {

    protected Context mHostContext;
    public BaseHookHandle(Context hostContext) {
        mHostContext = hostContext;
        init();
    }

    /**
     * 初始化，填充方法集合
     */
    protected abstract void init();

    /**
     * 所有的方法集合
     */
    protected Map<String, BaseHookMethodHandler> sHookedMethodHandlers = new HashMap<String, BaseHookMethodHandler>(5);

    /**
     * 获取该方法的实现
     * @param method
     * @return
     */
    public BaseHookMethodHandler getHookedMethodHandler(Method method) {
        if (method != null) {
            return sHookedMethodHandlers.get(method.getName());
        } else {
            return null;
        }
    }
}
