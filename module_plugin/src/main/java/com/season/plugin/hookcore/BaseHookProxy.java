package com.season.plugin.hookcore;

import android.content.Context;


import com.season.plugin.hookcore.handle.BaseHookHandle;
import com.season.plugin.hookcore.handle.BaseHookMethodHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * Disc: 代理基类，动态代理
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public abstract class BaseHookProxy extends BaseHook implements InvocationHandler {

    protected BaseHookHandle mHookHandles;
    public BaseHookProxy(Context hostContext) {
        super(hostContext);
        mHookHandles = createHookHandle();
    }

    protected Object mOldObj;

    /**
     * 设置默认的类方法
     * @param oldObj
     */
    public void setOldObj(Object oldObj) {
        this.mOldObj = oldObj;
    }

    /**
     * 绑定的代理
     * @return
     */
    protected abstract BaseHookHandle createHookHandle();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        BaseHookMethodHandler hookedMethodHandler = mHookHandles.getHookedMethodHandler(method);
        if (hookedMethodHandler != null) {
            return hookedMethodHandler.doHookInner(mOldObj, method, args);
        }
        return method.invoke(mOldObj, args);
    }
}
