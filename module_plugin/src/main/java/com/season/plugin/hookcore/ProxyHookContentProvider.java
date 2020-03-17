package com.season.plugin.hookcore;

import android.content.Context;
import android.content.pm.ProviderInfo;

import com.season.plugin.hookcore.handle.HookHandleContentProvider;
import com.season.plugin.hookcore.handle.BaseHookHandle;

/**
 * Disc: hook ContentProvider
 * 绑定的代理
 * @see HookHandleContentProvider
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class ProxyHookContentProvider extends BaseHookProxy {


    private final ProviderInfo mStubProvider;
    private final ProviderInfo mTargetProvider;
    private final boolean mLocalProvider;


    public ProxyHookContentProvider(Context context, Object oldObj, ProviderInfo stubProvider, ProviderInfo targetProvider, boolean localProvider) {
        super(context);
        setOldObj(oldObj);
        mStubProvider = stubProvider;
        mTargetProvider = targetProvider;
        mLocalProvider = localProvider;
        mHookHandles = createHookHandle();

    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new HookHandleContentProvider(mHostContext, mStubProvider, mTargetProvider, mLocalProvider);
    }

    @Override
    public void onInstall() throws Throwable {

    }
}
