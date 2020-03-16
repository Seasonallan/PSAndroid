package com.season.lib.file;

import android.content.res.AssetManager;
import android.text.TextUtils;
import com.season.lib.BaseContext;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 文件相关工具类
 */
public final class FileUtils {


    interface FileSuffix {
        String JPG = "jpg";
        String GIF = "gif";
        String PNG = "png";
        String MP4 = "mp4";
        String WEBP = "webp";
        String WAV = "wav";
        String MP3 = "mp3";
    }
    /**
     * 根据文件头判断文件类型
     */
    private static final HashMap<String, String> mFileTypes = new HashMap<String, String>();
    static {
        mFileTypes.put("FFD8FF", FileSuffix.JPG);
        mFileTypes.put("89504E47", FileSuffix.PNG);
        mFileTypes.put("47494638", FileSuffix.GIF);
        mFileTypes.put("000013", FileSuffix.WEBP);
        mFileTypes.put("474011", FileSuffix.MP4);
        mFileTypes.put("00000", FileSuffix.MP4);//MEPG
        mFileTypes.put("524946", FileSuffix.WAV);//wav
        mFileTypes.put("494433", FileSuffix.MP3);//wav
        //        mFileTypes.put("41564920", FileSuffix.MP4);//AVI
        //        mFileTypes.put("6D6F6F76", FileSuffix.MP4);//mov
        //        mFileTypes.put("57415645", FileSuffix.WAV);//wav
        //        mFileTypes.put("49492A00", "tif");
        //        mFileTypes.put("424D", "bmp");
        //        mFileTypes.put("41433130", "dwg"); //CAD
        //        mFileTypes.put("38425053", "psd");
        //        mFileTypes.put("7B5C727466", "rtf"); //日记本
        //        mFileTypes.put("3C3F786D6C", "xml");
        //        mFileTypes.put("68746D6C3E", "html");
        //        mFileTypes.put("44656C69766572792D646174653A", "eml"); //邮件
        //        mFileTypes.put("D0CF11E0", "doc");
        //        mFileTypes.put("5374616E64617264204A", "mdb");
        //        mFileTypes.put("252150532D41646F6265", "ps");
        //        mFileTypes.put("255044462D312E", "pdf");
        //        mFileTypes.put("504B0304", "zip");
        //        mFileTypes.put("52617221", "rar");
        //        mFileTypes.put("57415645", "wav");
        //        mFileTypes.put("41564920", "avi");
        //        mFileTypes.put("2E524D46", "rm");
        //        mFileTypes.put("000001BA", "mpg");
        //        mFileTypes.put("000001B3", "mpg");
        //        mFileTypes.put("6D6F6F76", "mov");
        //        mFileTypes.put("3026B2758E66CF11", "asf");
        //        mFileTypes.put("4D546864", "mid");
        //        mFileTypes.put("1F8B08", "gz");
        //        mFileTypes.put("", "");
    }



    /**
     * 把Assets里面得文件复制到 /data/data/files 目录下
     *
     * @param sourceName
     */
    public static File copyAssets(String sourceName) {
        AssetManager am = BaseContext.getInstance().getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = am.open(sourceName);
            File extractFile = BaseContext.getInstance().getFileStreamPath(sourceName);
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
            return extractFile;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }
        return null;
    }
    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable e) {
        }
    }

    public static boolean isGif(String type){
        return type.equalsIgnoreCase("gif");
    }
    /**
     * 判断图片是否是静止图片
     * @param filePath
     * @return
     */
    public static boolean isStaticImageFile(String filePath){
        String fileType = getFileType(filePath);
        return fileType.equals(FileSuffix.PNG) || fileType
                .equals(FileSuffix.JPG);
    }

    /**
     * 根据文件头获取文件类型
     * @param filePath
     * @return
     */
    public static String getFileType(String filePath) {
        String keySearch = getFileHeader(filePath);
        String fileSuffix = mFileTypes.get(keySearch);
        //补充 这里并不是所有的文件格式前10 byte（jpg）都一致，前五个byte一致即可
        if (TextUtils.isEmpty(fileSuffix)) {
            Iterator<String> keyList = mFileTypes.keySet().iterator();
            //出现过4位的文件，下标越界
            if (TextUtils.isEmpty(keySearch) || keySearch.length() < 5) {
                return "";
            } else {
                String key, keySearchPrefix = keySearch.substring(0, 5);
                while (keyList.hasNext()) {
                    key = keyList.next();
                    if (key.contains(keySearchPrefix)) {
                        fileSuffix = mFileTypes.get(key);
                        break;
                    }
                }
            }
        }
        if (TextUtils.isEmpty(fileSuffix)) {
            return FileSuffix.PNG;
        }
        return fileSuffix;
    }


    /**
     * 获取文件头信息
     * @param filePath
     * @return
     */
    public static String getFileHeader(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        File file = new File(filePath);
        if (!file.exists() || file.length() < 4) {
            return "";
        }
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(file);
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * 判断某个图片是否GIF格式
     *
     * @param path 图片路径
     * @return 图片是GIF格式返回true, 否则返回false
     */
    public static boolean isGifFile(String path) {
        final int HEAD_COUNT = 3; //gif扩展名的长度
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        boolean isGif = false;
        InputStream stream = null;
        try {
            stream = new FileInputStream(path);
            byte[] head = new byte[HEAD_COUNT];
            stream.read(head);
            String imgType = new String(head);
            isGif = imgType.equalsIgnoreCase("gif");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isGif;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }


    /**
     * 存储Serializable类型数据结构到文件
     * @param key
     * @param value
     */
    public static void saveSerialData(String key, Serializable value){

        FileOutputStream fileOutputStream=null;
        ObjectOutputStream objectOutputStream =null;
        try {
            File file = new File(BaseContext.getInstance().getCacheDir(), key);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream= new FileOutputStream(file.toString());
            objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(value);

        } catch (Exception e) {
        }finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取Serializable类型数据结构
     * @param key
     * @return
     */
    public static Object getSerialData(String key){

        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            File file = new File(BaseContext.getInstance().getCacheDir(), key);
            fileInputStream = new FileInputStream(file.toString());
            objectInputStream = new ObjectInputStream(fileInputStream);

            return objectInputStream.readObject();

        } catch (Exception e) {
        }finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 将字符串写入文件
     * @param str
     * @param filePath
     * @return
     */
    public static boolean writeStr2File(String str, String filePath) {
        return writeStr2File(str, filePath, Integer.MAX_VALUE);
    }

    public static boolean writeStr2File(String str, String filePath,
                                         int maxSize) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (maxSize < file.length()) {
                    file.delete();
                }
            }
            FileWriter fw = new FileWriter(filePath, true);
            fw.write(str);
            fw.flush();
            fw.close();
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 复制流输入到文件
     * @param path
     * @param is
     * @return
     */
    public static boolean copyFileToFile(String path, InputStream is) {
        try {
            File file = new File(path);
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(file);
            int bufferSize = 4096;
            byte[] b = new byte[bufferSize];
            int nRead;
            int currentBytes = 0;
            int bytesNotified = currentBytes;
            long timeLastNotification = 0;
            for (;;) {
                nRead = bis.read(b, 0, bufferSize);
                if (nRead == -1) {
                    break;
                }
                currentBytes += nRead;
                fos.write(b, 0, nRead);
                long now = System.currentTimeMillis();
                if (currentBytes - bytesNotified > bufferSize
                        && now - timeLastNotification > 1500) {
                    bytesNotified = currentBytes;
                    timeLastNotification = now;
                }
            }
            fos.flush();
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 删除目录
     *
     * @param dir 目录
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteDir(File dir) {
        if (dir == null) return false;
        // 目录不存在返回true
        if (!dir.exists()) return true;
        // 不是目录返回false
        if (!dir.isDirectory()) return false;
        // 现在文件存在且是文件夹
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 删除文件
     *
     * @param file 文件
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteFile(final File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }



    /**
     * 获取文件最后修改的毫秒时间戳
     *
     * @param file 文件
     * @return 文件最后修改的毫秒时间戳
     */
    public static long getFileLastModified(final File file) {
        if (file == null) return -1;
        return file.lastModified();
    }

    /**
     * 简单获取文件编码格式
     *
     * @param file 文件
     * @return 文件编码
     */
    public static String getFileCharsetSimple(final File file) {
        int p = 0;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            p = (is.read() << 8) + is.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        switch (p) {
            case 0xefbb:
                return "UTF-8";
            case 0xfffe:
                return "Unicode";
            case 0xfeff:
                return "UTF-16BE";
            default:
                return "GBK";
        }
    }


}
