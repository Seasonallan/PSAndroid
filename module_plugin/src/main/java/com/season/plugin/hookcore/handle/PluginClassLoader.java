
package com.season.plugin.hookcore.handle;
import dalvik.system.DexClassLoader;


/**
 * Disc: h适配奇酷手机青春版。
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-17 10:07
 */
public class PluginClassLoader extends DexClassLoader {

    public PluginClassLoader(String apkfile, String optimizedDirectory, String libraryPath, ClassLoader systemClassLoader) {
        super(apkfile, optimizedDirectory, libraryPath, systemClassLoader);
    }

}
