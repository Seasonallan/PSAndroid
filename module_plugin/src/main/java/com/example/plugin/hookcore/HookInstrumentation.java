/*
**        DroidPlugin Project
**
** Copyright(c) 2015 Andy Zhang <zhangyong232@gmail.com>
**
** This file is part of DroidPlugin.
**
** DroidPlugin is free software: you can redistribute it and/or
** modify it under the terms of the GNU Lesser General Public
** License as published by the Free Software Foundation, either
** version 3 of the License, or (at your option) any later version.
**
** DroidPlugin is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public
** License along with DroidPlugin.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
**
**/

package com.example.plugin.hookcore;

import android.app.Instrumentation;
import android.content.Context;

import com.example.lib.util.LogUtil;
import com.example.plugin.compat.ActivityThreadCompat;
import com.example.plugin.hookcore.handle.PluginInstrumentation;
import com.example.lib.reflect.FieldUtils;

import java.lang.reflect.Field;

/**
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/2.
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
