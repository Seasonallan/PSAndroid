package com.season.plugin.stub.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Disc: 缓存Window属性，判断window是否是一下三种类型，windowIsTranslucent，windowIsFloating，windowShowWallpaper。
 * 如果是的话Activity的选择会使用与定义的Dialog类型。以后在选择Activity的时候会用到。
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-22 13:34
 */
public final class AttributeCache {
    private static AttributeCache sInstance = null;

    private final Context mContext;
    private final WeakHashMap<String, Package> mPackages =
            new WeakHashMap<String, Package>();
    private final Configuration mConfiguration = new Configuration();

    public final static class Package {
        public final Context context;
        private final SparseArray<HashMap<int[], Entry>> mMap
                = new SparseArray<HashMap<int[], Entry>>();

        public Package(Context c) {
            context = c;
        }
    }

    public final static class Entry {
        public final Context context;
        public final TypedArray array;

        public Entry(Context c, TypedArray ta) {
            context = c;
            array = ta;
        }
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new AttributeCache(context);
        }
    }

    public static AttributeCache instance() {
        return sInstance;
    }

    public AttributeCache(Context context) {
        mContext = context;
    }

    public void removePackage(String packageName) {
        synchronized (this) {
            mPackages.remove(packageName);
        }
    }

    public void updateConfiguration(Configuration config) {
        synchronized (this) {
            int changes = mConfiguration.updateFrom(config);
            if ((changes & ~(ActivityInfo.CONFIG_FONT_SCALE |
                    ActivityInfo.CONFIG_KEYBOARD_HIDDEN |
                    ActivityInfo.CONFIG_ORIENTATION)) != 0) {
                // The configurations being masked out are ones that commonly
                // change so we don't want flushing the cache... all others
                // will flush the cache.
                mPackages.clear();
            }
        }
    }

    public Entry get(String packageName, int resId, int[] styleable) {
        synchronized (this) {
            Package pkg = mPackages.get(packageName);
            HashMap<int[], Entry> map = null;
            Entry ent = null;
            if (pkg != null) {
                map = pkg.mMap.get(resId);
                if (map != null) {
                    ent = map.get(styleable);
                    if (ent != null) {
                        return ent;
                    }
                }
            } else {
                Context context;
                try {
                    context = mContext.createPackageContext(packageName, 0);
                    if (context == null) {
                        return null;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    return null;
                }
                pkg = new Package(context);
                mPackages.put(packageName, pkg);
            }

            if (map == null) {
                map = new HashMap<int[], Entry>();
                pkg.mMap.put(resId, map);
            }

            try {
                ent = new Entry(pkg.context,
                        pkg.context.obtainStyledAttributes(resId, styleable));
                map.put(styleable, ent);
            } catch (Resources.NotFoundException e) {
                return null;
            }

            return ent;
        }
    }
}

