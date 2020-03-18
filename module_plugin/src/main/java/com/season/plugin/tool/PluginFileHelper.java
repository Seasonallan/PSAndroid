package com.season.plugin.tool;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Disc: 插件文件目录
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-22 13:34
 */
public class PluginFileHelper {


    private static File sBaseDir = null;

    private static void init(Context context) {
        if (sBaseDir == null) {
            sBaseDir = new File(context.getCacheDir().getParentFile(), "plugin");
            enforceDirExists(sBaseDir);
        }
    }

    private static String enforceDirExists(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getPath();
    }

    public static String makePluginBaseDir(Context context, String pluginInfoPackageName) {
        init(context);
        return enforceDirExists(new File(sBaseDir, pluginInfoPackageName));
    }

    public static String getBaseDir(Context context) {
        init(context);
        return enforceDirExists(sBaseDir);
    }

    public static String getPluginDataDir(Context context, String pluginInfoPackageName) {
        return enforceDirExists(new File(makePluginBaseDir(context, pluginInfoPackageName), "data/" + pluginInfoPackageName));
    }

    public static String getPluginSignatureDir(Context context, String pluginInfoPackageName) {
        return enforceDirExists(new File(makePluginBaseDir(context, pluginInfoPackageName), "Signature/"));
    }

    public static String getPluginSignatureFile(Context context, String pluginInfoPackageName, int index) {
        return new File(getPluginSignatureDir(context, pluginInfoPackageName), String.format("Signature_%s.key", index)).getPath();
    }

    public static List<String> getPluginSignatureFiles(Context context, String pluginInfoPackageName) {
        ArrayList<String> files = new ArrayList<String>();
        String dir = getPluginSignatureDir(context, pluginInfoPackageName);
        File d = new File(dir);
        File[] fs = d.listFiles();
        if (fs != null && fs.length > 0) {
            for (File f : fs) {
                files.add(f.getPath());
            }
        }
        return files;
    }

    public static String getPluginApkDir(Context context, String pluginInfoPackageName) {
        return enforceDirExists(new File(makePluginBaseDir(context, pluginInfoPackageName), "apk"));
    }

    public static String getPluginApkFile(Context context, String pluginInfoPackageName) {
        return new File(getPluginApkDir(context, pluginInfoPackageName), "base-1.apk").getPath();
    }

    public static String getPluginDalvikCacheDir(Context context, String pluginInfoPackageName) {
        return enforceDirExists(new File(makePluginBaseDir(context, pluginInfoPackageName), "dalvik-cache"));
    }

    public static String getPluginNativeLibraryDir(Context context, String pluginInfoPackageName) {
        return enforceDirExists(new File(makePluginBaseDir(context, pluginInfoPackageName), "lib"));
    }


    public static String getPluginDalvikCacheFile(Context context, String pluginInfoPackageName) {
        String dalvikCacheDir = getPluginDalvikCacheDir(context, pluginInfoPackageName);

        String pluginApkFile = getPluginApkFile(context, pluginInfoPackageName);
        String apkName = new File(pluginApkFile).getName();
        String dexName = apkName.replace(File.separator, "@");
        if (dexName.startsWith("@")) {
            dexName = dexName.substring(1);
        }
        return new File(dalvikCacheDir, dexName + "@classes.dex").getPath();
    }

    public static String getContextDataDir(Context context) {
        String dataDir = new File(Environment.getDataDirectory(), "data/").getPath();
        return new File(dataDir, context.getPackageName()).getPath();
    }

    public static void cleanOptimizedDirectory(String optimizedDirectory) {
        try {
            File dir = new File(optimizedDirectory);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }

            if (dir.exists() && dir.isFile()) {
                dir.delete();
                dir.mkdirs();
            }
        } catch (Throwable e) {
        }
    }


    public static void copyFile(String src, String dst) throws IOException {
        BufferedInputStream in = null;
        BufferedOutputStream ou = null;
        try {
            in = new BufferedInputStream(new FileInputStream(src));
            ou = new BufferedOutputStream(new FileOutputStream(dst));
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                ou.write(buffer, 0, read);
            }
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

    public static void deleteDir(String file) {
        deleteFile(new File(file));
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        }
        file.delete();
    }

    public static void writeToFile(File file, byte[] data) throws IOException {
        FileOutputStream fou = null;
        try {
            fou = new FileOutputStream(file);
            fou.write(data);
        } finally {
            if (fou != null) {
                try {
                    fou.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static byte[] readFromFile(File file) throws IOException {
        FileInputStream fin = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            fin = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = fin.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            byte[] data = out.toByteArray();
            out.close();
            return data;
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                }
            }
        }
    }


}
