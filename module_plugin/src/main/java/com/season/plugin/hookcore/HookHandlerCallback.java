package com.season.plugin.hookcore;

import android.content.Context;
import android.os.Handler;

import com.season.lib.util.LogUtil;
import com.season.plugin.compat.ActivityThreadCompat;
import com.season.lib.support.reflect.FieldUtils;
import com.season.plugin.hookcore.handle.PluginCallback;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Disc:  hook Callback处理假的activity启动后替换为原本的activity并加载application和ClassLoader的替换
 *  绑定的代理
 * @see PluginCallback
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:29
 */
public class HookHandlerCallback extends BaseHook {

    private static final String TAG = HookHandlerCallback.class.getSimpleName();
    private List<PluginCallback> mCallbacks = new ArrayList<PluginCallback>(1);

    public HookHandlerCallback(Context hostContext) {
        super(hostContext);
    }

    @Override
    public void onInstall() throws Throwable {
        Object target = ActivityThreadCompat.currentActivityThread();
        Class ActivityThreadClass = ActivityThreadCompat.activityThreadClass();

        /*替换ActivityThread.mH.mCallback，拦截组件调度消息*/
        Field mHField = FieldUtils.getField(ActivityThreadClass, "mH");
        Handler handler = (Handler) FieldUtils.readField(mHField, target);
        Field mCallbackField = FieldUtils.getField(Handler.class, "mCallback");
        //*这里读取出旧的callback并处理*/
        Object mCallback = FieldUtils.readField(mCallbackField, handler);
        if (!PluginCallback.class.isInstance(mCallback)) {
            PluginCallback value = mCallback != null ? new PluginCallback(mHostContext, handler, (Handler.Callback) mCallback) : new PluginCallback(mHostContext, handler, null);
            mCallbacks.add(value);
            FieldUtils.writeField(mCallbackField, handler, value);
            LogUtil.i(TAG, "PluginCallbackHook has installed");
        } else {
            LogUtil.i(TAG, "PluginCallbackHook has installed,skip");
        }
    }
}
