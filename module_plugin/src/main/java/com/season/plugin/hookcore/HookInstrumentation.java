package com.season.plugin.hookcore;

import android.app.Instrumentation;
import android.content.Context;

import com.season.lib.util.LogUtil;
import com.season.plugin.compat.ActivityThreadCompat;
import com.season.plugin.hookcore.handle.PluginInstrumentation;
import com.season.lib.reflect.FieldUtils;

import java.lang.reflect.Field;


/**
 * Disc: hook Instrumentation处理生命周期，在替身上启动相关的activity方法，并伪装系统服务
 * 绑定的代理
 * @see PluginInstrumentation
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class HookInstrumentation extends BaseHook {

    private static final String TAG = HookInstrumentation.class.getSimpleName();
    public HookInstrumentation(Context hostContext) {
        super(hostContext);
    }

    @Override
    public void onInstall() throws Throwable {
        Object target = ActivityThreadCompat.currentActivityThread();
        Class ActivityThreadClass = ActivityThreadCompat.activityThreadClass();

         /*替换ActivityThread.mInstrumentation，拦截组件调度消息*/
        Field mInstrumentationField = FieldUtils.getField(ActivityThreadClass, "mInstrumentation");
        Instrumentation mInstrumentation = (Instrumentation) FieldUtils.readField(mInstrumentationField, target);
        if (!PluginInstrumentation.class.isInstance(mInstrumentation)) {
            PluginInstrumentation pit = new PluginInstrumentation(mHostContext, mInstrumentation);
            FieldUtils.writeField(mInstrumentationField, target, pit);
            LogUtil.i(TAG, "Install Instrumentation BaseHook old=%s,new=%s", mInstrumentationField, pit);
        } else {
            LogUtil.i(TAG, "Instrumentation has installed,skip");
        }
    }
}
