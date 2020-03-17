package com.season.plugin.hookcore;

import android.content.Context;

/**
 * Disc: 代理基类，默认静态
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public abstract class BaseHook {

    protected Context mHostContext;
    protected BaseHook(Context hostContext) {
        mHostContext = hostContext;
    }

    /**
     * 启动hook
     * @throws Throwable
     */
    public abstract void onInstall() throws Throwable;

}
