package com.season.plugin.tool;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

import com.season.lib.util.LogUtil;
import com.season.plugin.compat.VMRuntimeCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Disc: so文件 ，32位64位判断
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-22 13:34
 */
public class SoFileHelper {

    private static final String TAG = SoFileHelper.class.getSimpleName();

    /**
     * 拷贝so文件
     * @param context
     * @param apkfile
     * @param applicationInfo
     * @throws Exception
     */
    public static void copyNativeLibs(Context context, String apkfile, ApplicationInfo applicationInfo) throws Exception {
        String nativeLibraryDir = PluginFileHelper.getPluginNativeLibraryDir(context, applicationInfo.packageName);
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkfile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            Map<String, ZipEntry> libZipEntries = new HashMap<String, ZipEntry>();
            Map<String, Set<String>> soList = new HashMap<String, Set<String>>(1);
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("../")) {
                    LogUtil.i(TAG, "Path traversal attack prevented");
                    continue;
                }
                if (name.startsWith("lib/") && !entry.isDirectory()) {
                    libZipEntries.put(name, entry);
                    String soName = new File(name).getName();
                    Set<String> fs = soList.get(soName);
                    if (fs == null) {
                        fs = new TreeSet<String>();
                        soList.put(soName, fs);
                    }
                    fs.add(name);
                }
            }

            for (String soName : soList.keySet()) {
                LogUtil.e(TAG, "try so =" + soName);
                Set<String> soPaths = soList.get(soName);
                String soPath = findSoPath(soPaths, soName);
                if (soPath != null) {
                    File file = new File(nativeLibraryDir, soName);
                    if (file.exists()) {
                        file.delete();
                    }
                    InputStream in = null;
                    FileOutputStream ou = null;
                    try {
                        in = zipFile.getInputStream(libZipEntries.get(soPath));
                        ou = new FileOutputStream(file);
                        byte[] buf = new byte[8192];
                        int read = 0;
                        while ((read = in.read(buf)) != -1) {
                            ou.write(buf, 0, read);
                        }
                        ou.flush();
                        ou.getFD().sync();
                        LogUtil.i(TAG, "copy so(%s) for %s to %s ok!", soName, soPath, file.getPath());
                    } catch (Exception e) {
                        if (file.exists()) {
                            file.delete();
                        }
                        throw e;
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Exception e) {
                            }
                        }
                        if (ou != null) {
                            try {
                                ou.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                }
            }
        }
    }


    /**
     * 选择so文件目录
     * @param soPaths
     * @param soName
     * @return
     */
    public static String findSoPath(Set<String> soPaths, String soName) {
        if (soPaths != null && soPaths.size() > 0) {
            if (VMRuntimeCompat.is64Bit()) {
                //在宿主程序运行在64位进程中的时候，插件的so也只拷贝64位，否则会出现不支持的情况。
                String[] supported64BitAbis = SoFileHelper.SUPPORTED_64_BIT_ABIS;
                Arrays.sort(supported64BitAbis);
                for (String soPath : soPaths) {
                    String abi = soPath.replaceFirst("lib/", "");
                    abi = abi.replace("/" + soName, "");

                    if (!TextUtils.isEmpty(abi) && Arrays.binarySearch(supported64BitAbis, abi) >= 0) {
                        return soPath;
                    }
                }
            } else {
                //在宿主程序运行在32位进程中的时候，插件的so也只拷贝64位，否则会出现不支持的情况。
                String[] supported32BitAbis = SoFileHelper.SUPPORTED_32_BIT_ABIS;
                Arrays.sort(supported32BitAbis);
                for (String soPath : soPaths) {
                    String abi = soPath.replaceFirst("lib/", "");
                    abi = abi.replace("/" + soName, "");
                    if (!TextUtils.isEmpty(abi) && Arrays.binarySearch(supported32BitAbis, abi) >= 0) {
                        return soPath;
                    }
                }
            }
        }
        return null;
    }

    public final static String[] SUPPORTED_ABIS;

    public final static String[] SUPPORTED_32_BIT_ABIS;

    public static final String[] SUPPORTED_64_BIT_ABIS;

    static {
        //init SUPPORTED_ABIS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.SUPPORTED_ABIS != null) {
                SUPPORTED_ABIS = new String[Build.SUPPORTED_ABIS.length];
                System.arraycopy(Build.SUPPORTED_ABIS, 0, SUPPORTED_ABIS, 0, SUPPORTED_ABIS.length);
            } else {
                SUPPORTED_ABIS = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            }
        } else {
            SUPPORTED_ABIS = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }

        //init SUPPORTED_32_BIT_ABIS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.SUPPORTED_32_BIT_ABIS != null) {
                SUPPORTED_32_BIT_ABIS = new String[Build.SUPPORTED_32_BIT_ABIS.length];
                System.arraycopy(Build.SUPPORTED_32_BIT_ABIS, 0, SUPPORTED_32_BIT_ABIS, 0, SUPPORTED_32_BIT_ABIS.length);
            } else {
                SUPPORTED_32_BIT_ABIS = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            }
        } else {
            SUPPORTED_32_BIT_ABIS = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }

        //init SUPPORTED_64_BIT_ABIS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.SUPPORTED_64_BIT_ABIS != null) {
                SUPPORTED_64_BIT_ABIS = new String[Build.SUPPORTED_64_BIT_ABIS.length];
                System.arraycopy(Build.SUPPORTED_64_BIT_ABIS, 0, SUPPORTED_64_BIT_ABIS, 0, SUPPORTED_64_BIT_ABIS.length);
            } else {
                SUPPORTED_64_BIT_ABIS = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            }
        } else {
            SUPPORTED_64_BIT_ABIS = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
    }
}
