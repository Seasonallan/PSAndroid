package com.season.plugin.hookcore.handle;

import android.content.Context;
import com.season.lib.util.LogUtil;
import java.lang.reflect.Method;

/**
 * Disc: hook预处理handler
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class BaseHookMethodHandler {

    private static final String TAG = BaseHookMethodHandler.class.getSimpleName();
    protected final Context mHostContext;

    private Object mFakedResult = null;
    private boolean mUseFakedResult = false;

    public BaseHookMethodHandler(Context hostContext) {
        this.mHostContext = hostContext;
    }


    /**
     * 动态代理对方法的处理
     * @param receiver
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public synchronized Object doHookInner(Object receiver, Method method, Object[] args) throws Throwable {
        long b = System.currentTimeMillis();
        try {
            mUseFakedResult = false;
            mFakedResult = null;
            boolean suc = beforeInvoke(receiver, method, args);
            Object invokeResult = null;
            if (!suc) {
                invokeResult = method.invoke(receiver, args);
            }
            afterInvoke(receiver, method, args, invokeResult);
            if (mUseFakedResult) {
                return mFakedResult;
            } else {
                return invokeResult;
            }
        } finally {
            long time = System.currentTimeMillis() - b;
            if (time > 0) {
                LogUtil.i(TAG, "doHookInner method(%s.%s) cost %s ms", method.getDeclaringClass().getName(), method.getName(), time);
            }
        }
    }

    /**
     * 状态保持
     * @param fakedResult
     */
    public void setFakedResult(Object fakedResult) {
        this.mFakedResult = fakedResult;
        this.mUseFakedResult = true;
    }

    /**
     * 在某个方法被调用之前执行，如果返回true，则不执行原始的方法，否则执行原始方法
     */
    protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
        return false;
    }

    /**
     * 执行完方法之后对状态进行控制
     */
    protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
    }

}
