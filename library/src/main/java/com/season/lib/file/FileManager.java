package com.season.lib.file;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.season.lib.util.Constant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FileManager implements IFileManager {
    public static Context context;
    public static String videoPath = Environment.getExternalStorageDirectory().getPath();

    public FileManager() {
    }

    private volatile static FileManager singleton;

    public static FileManager getSingleton() {
        if (singleton == null) {
            synchronized (FileManager.class) {
                if (singleton == null) {
                    singleton = new FileManager();
                }
            }
        }
        return singleton;
    }

    /**
     * 与Constant类的CameraFileNameTag对应
     * <p>
     * String NORMAL_VIDEO = "normalvideo.mp4";
     * String REVERSE = "reverse.mp4";
     * String PINGPANG ="pingpang.mp4";
     * String CROP_VIDEO_SIZE ="crop.mp4";
     * String RESOLUTION_VIDEO ="resolution.mp4";
     * String MERGE_VIDEO ="merge.mp4";
     * String OVERLAY ="overlay.png";
     * String OVERLAY4VIDEO ="overlay4video.png";
     * String FINALGIF ="final.gif";
     * String NORMAL_PHOTO ="normalphoto.png";
     * String MERGE_PHOTO ="mergephoto.png";
     * String FINALPHOTO ="finalphoto.png";
     */
    public static String getCurrentTime() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        return timeStamp;
    }

    public static String getCurrentTimeDraft() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINESE).format(new Date());
        return timeStamp;
    }

    /**
     * 如果发送失败就保存在草稿
     * https://img.biaoqing.com/AndroidTest1502097832242_0 原测试命名格式
     * androidTest/work/20170718/10263301148.gif 测试命名格式
     * work/20170718/10263301148.gif 正式命名格式
     * 作品：
     * work/20180101/文件MD5.后缀名
     * <p>
     * 评论
     * upfile/20180101/文件MD5.后缀名
     */
    public static String getCurrentTime4Publish() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd/HHmmss", Locale.CHINESE).format(new Date());
        return timeStamp;
    }

    public static String getCurrentTime5Publish() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.CHINESE).format(new Date());
        return timeStamp;
    }

    /**
     * private String savePath = "/uploads/{year}{mon}{day}/{random32}{.suffix}";
     * 类型	格式	说明
     * 绝对值	String	指定具体的路径，如: /path/to/file.txt
     * 时间类	{year} {mon} {day} {hour} {min} {sec}	日期、时间相关内容（UTC 时间）
     * md5 类	{filemd5}	文件的 md5 值
     * 随机类	{random} {random32}	16 位或 32 位随机字符和数字
     * 文件名	{filename} {suffix} {.suffix}	上传文件的文件名及扩展名
     */
    public static String getCurrentTime4PublishUpyun() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd/HHmmss", Locale.CHINESE).format(new Date());
        return timeStamp;
    }

    //得到内部缓存路径
    public static File getCacheFileParent(String type) {
        File file = new File(context.getCacheDir(), type);
        return file;
    }

    public static File getBitmapFromStickerLayeroutputFile(int type) {
        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant
                .FileManager.BQ_Separator), Constant.FileManager.Bitmap);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "Merge_VID_" + timeStamp + ".mp4");
        } else if (type == 2) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "GIf_" + timeStamp + ".gif");
        } else {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "Bitmap_" + timeStamp + ".png");
        }
        return mediaFile;
    }

    public static File getVideoCompressFile() {
        File mediaStorageDir;
        mediaStorageDir = getCameraFileDir();
        mediaStorageDir.mkdirs();
        return new File(mediaStorageDir.getPath() + File.separator + "compress_" + System.currentTimeMillis() + ".mp4");
    }

    public static File getVideoToGif_ImageFile() {
        File mediaStorageDir;
        mediaStorageDir = getCameraFileDir();
        mediaStorageDir.mkdirs();
        return new File(mediaStorageDir.getPath() + File.separator + "static_img_" + System.currentTimeMillis() + ".jpg");
    }

    public static File getCameraFileName(int type) {
        File mediaStorageDir;
        mediaStorageDir = getCameraFileDir();
        mediaStorageDir.mkdirs();
        File mediaFile = null;
        //TODO 改成枚举类型，更不容易犯错，待重构
        switch (type) {
            case 1:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.NORMAL_VIDEO);
                break;
            case 2:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.REVERSE);
                break;
            case 3:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.PINGPANG);
                break;
            case 4:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.CROP_VIDEO_SIZE);
                break;
            case 5:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.RESOLUTION_VIDEO);
                break;
            case 6:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.MERGE_VIDEO);
                break;
            case 7:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.OVERLAY);
                break;
            case 8:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.OVERLAY4VIDEO);
                break;
            case 9:
                File mediaStorageDir2 = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES +
                        Constant.FileManager.BQ_Separator), Constant.FileManager.Share);
                mediaFile = new File(mediaStorageDir2.getPath() + File.separator + getCurrentTime() + Constant.CameraFileName
                        .FINALGIF);
                break;
            case 10:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.NORMAL_PHOTO);
                break;
            case 11:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.MERGE_PHOTO);
                break;
            case 12:
                File mediaStorageDir3 = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES +
                        Constant.FileManager.BQ_Separator), Constant.FileManager.Share);
                mediaFile = new File(mediaStorageDir3.getPath() + File.separator + getCurrentTime() + Constant.CameraFileName
                        .FINALPHOTO);
                break;
            case 13:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.CROP_VIDEO_DURATION);
                break;
            case 14:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.SPEED_VIDEO);
                break;
            case 15:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.GIF_COLOUR_BOARD);
                break;
            case 16:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.IMG_WATERMARK_IOS_ORIGINAL);
                break;
            case 17:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.IMG_WATERMARK_IOS_ADJUST);
                break;
            case 18:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.NORMAL_VIDEO_PLUS_KEY_FRAME);
                break;
            case 19:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.POSITIVE_TS);
                break;
            case 20:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.REVERSE_TS);
                break;
            case 21:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.TEST_BITMAP);
                break;
            case 22:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.TIME_VIDEO);
                break;
            case 23:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.TIME_VIDEO_CONTACT_TEXT);
                break;
            case 24:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.OVERLAY_VIDEO);
                break;
            case 25:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.LAYER_GIF);
                break;
            case 26:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.MERGE_VIDEO4GIF);
                break;
            case 27:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + getCurrentTime4Publish() + "video_" + Constant
                        .CameraFileName.DRAFT_VIDEO);
                break;
            case 28:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + getCurrentTime4Publish() + "video_asset_" +
                        Constant.CameraFileName.DRAFT_VIDEO_ASSET);
                break;
            case 29:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.MUX_AV_VIDEO);
                break;
            case 30:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + getCurrentTime4Publish() + Constant.CameraFileName
                        .VIDEO_THUMB);
                break;
            case 32:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.CameraAdjustDuration);
                break;
            case 33:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + Constant.CameraFileName.AudioFromShotVideo);
                break;
        }
        return mediaFile;
    }

    /**
     * 得到拍摄文件存放的文件夹
     *
     * @return
     */
    public static File getCameraFileDir() {
        File mediaStorageDir;
        mediaStorageDir = getCacheFileParent(Constant.FileManager.Camera);
        return mediaStorageDir;
    }

    public static File getCameraFileName4Split(int num) {
        File mediaStorageDir;
        mediaStorageDir = getCameraFileDir();
        mediaStorageDir.mkdirs();
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "normalvideo" + num + ".mp4");
        return mediaFile;
    }

    /**
     * 得到贴纸下载路径
     *
     * @return
     */
    public static File getStickerDir() {
        File mediaStorageDir;
        mediaStorageDir = getCacheFileParent(Constant.FileManager.STICKER);
        mediaStorageDir.mkdirs();
        return mediaStorageDir;
    }


    private static File getDiyDir(Context context) {
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant
        // .FileManager.BQ_Separator), "DIY");
        File mediaStorageDir;
        mediaStorageDir = getDirectory(context.getCacheDir(), Constant.FileManager.DIY);
        return mediaStorageDir;
    }

    public static String getXunFeiFile() {
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant
        // .FileManager.BQ_Separator), "DIY");
//        File mediaStorageDir = new File(BaseApplication.getInstance().getCacheDir(), Constant.FileManager.DIY);
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/表情说说") + "/iat.wav";
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                return null;
//            }
//        }
//        return mediaStorageDir;
    }

    //语音合成得到的文件
    public static String getXunFeiTextAuidoFile() {
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant
        // .FileManager.BQ_Separator), "DIY");
//        File mediaStorageDir = new File(BaseApplication.getInstance().getCacheDir(), Constant.FileManager.DIY);
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/表情说说") + "/tts.wav";
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                return null;
//            }
//        }
//        return mediaStorageDir;
    }

    public static File getDiyLayerDir(Context context) {
        File parentFile = getDiyDir(context);
        if (parentFile == null) {
            return null;
        }
        File fileDir = getDirectory(parentFile, Constant.FileManager.Layer);
        return fileDir;
    }

    public static File getDiyBackgroundFile(Context context, String name, String type) {
        return getDiyFile(context,Constant.FileManager.Background, name, type);
    }

    public static File getDiyDownloadFile(Context context, String name, String type) {
        return getDiyFile(context,Constant.FileManager.Download, name, type);
    }

    public static File getDiyCropFile(Context context) {
        return getDiyFile(context,Constant.FileManager.Crop, null, "png");
    }

    public static File getDiyMaterialFile(Context context, String name, String type) {
        return getDiyFile(context,Constant.FileManager.Material, name, type);
    }

    public static File getDiyLayerFile(Context context, String name, String type) {
        return getDiyFile(context,Constant.FileManager.Layer, name, type);
    }

    public static File getDiyFontFile(Context context, String name) {
        return getDiyFile(context,Constant.FileManager.Font, name, "ttf");
    }

    public static File getDiyShareFile(Context context, String name, String type) {
        return getDiyFile(context,Constant.FileManager.Share, name, type);
    }

    public static File getCameraBitmapFileDir() {
        File mediaStorageDir = getDirectoryContainDebug(Constant.FileManager.CAMERA_BITMAP);
        return mediaStorageDir;
    }

    public static File getCameraMusicFile(int musicId) {
        File mediaStorageDir;
        mediaStorageDir = getCacheFileParent(Constant.FileManager.CAMERA_MUSIC);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "musicid_" + musicId + ".mp3");
        return mediaFile;
    }

    public static File getFollotShot(String url) {
        File mediaStorageDir;
        mediaStorageDir = getCacheFileParent(Constant.FileManager.FollotShotVideo);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "followshot_" + url.hashCode() + ".mp4");

        return mediaFile;
    }

    public static File getCameraMusicVideoFile(int musicId) {
        File mediaStorageDir;
        mediaStorageDir = getCacheFileParent(Constant.FileManager.CAMERA_MUSIC);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "amp4id_" + musicId + ".mp4");
        return mediaFile;
    }

    /**
     * 把gif的图片，先放到一个文件夹里面，然后再用ffmpeg生成gif
     *
     * @return
     */
    public static File getGifLayerFile() {
        File mediaStorageDir;
        mediaStorageDir = getDirectoryContainDebug(Constant.FileManager.GIF_LAYER);
        return mediaStorageDir;
    }


    public static String getGifLayerName(int i) {
        String format = String.format("%03d", i);
        String s = getGifLayerFile() + File.separator + "layer-" + format + ".png";
        return s;
    }

    private static File getDiyFile(Context context, String dir, String name, String type) {
        File parentFile = getDiyDir(context);
        if (parentFile == null) {
            return null;
        }
        File mediaStorageDir = getDirectory(parentFile, dir);
        if (TextUtils.isEmpty(name)) {
            name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        }
        return new File(mediaStorageDir, name + "." + type);
    }

    public static File getCameraSaveLocalAlbumFile(int tag) {
//        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constant
//                .FileManager.BQ);
        //部分手机需要存到这里，才能被相册找到，比如VIVO
        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = null;
        if (tag == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else if (tag == 2) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        } else if (tag == 3) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "GIF_" + timeStamp + ".gif");
        }
        return mediaFile;
    }

    /**
     * 部分手机需要存到这里，才能被相册找到，比如VIVO
     *
     * @param tag
     * @return
     */
    public static File getSaveLocalVideoFile(int tag) {
        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = null;
        if (tag == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else if (tag == 2) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        } else if (tag == 3) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "GIF_" + timeStamp + ".gif");
        }
        return mediaFile;
    }

    public static File getImageSaveLocalAlbumFile(String type) {
//        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constant
//                .FileManager.BQ);
        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + Calendar.getInstance().getTimeInMillis() + type);

        return mediaFile;
    }

    public static File getShareLocalFile(String type) {
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant.FileManager
                .BQ_Separator);
        mediaStorageDir.mkdirs();
        //表情说说目录下
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + Calendar.getInstance().getTimeInMillis() + type);
    }

    public static File getDiyFile(Context context, String type) {
        File mediaStorageDir = context.getCacheDir();
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + Calendar.getInstance().getTimeInMillis() + type);
    }

    public static File getShareFile(String type) {
        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/表情说说"),
                "Share");
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + Calendar.getInstance().getTimeInMillis() + type);
    }

    /**
     * 下载webp原图到缓存路径
     *
     * @param type
     * @return
     */
    public static File getCacheFile(String type) {
        File mediaStorageDir = context.getExternalCacheDir();
        File mediaFile;

        if (mediaStorageDir == null) {
            return null;
        }

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + Calendar.getInstance().getTimeInMillis() + type);

        return mediaFile;
    }

    /**
     * 表情详情视频缓存的路径
     * 获取视频缓存路径
     *
     * @param url
     * @return
     */
    public static File getVideoCacheFile(String url) {
        File mediaStorageDir = context.getExternalCacheDir();
        File mediaFile;

        if (mediaStorageDir == null) {
            return null;
        }

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "video_cache" + File.separator + url.hashCode() +
                "" + ".mp4");

        return mediaFile;
    }

    /**
     * 获取视频临时文件
     *
     * @param url
     * @return
     */
    public static File getVideoTempFile(String url) {
        File mediaStorageDir = context.getExternalCacheDir();
        File mediaFile;

        if (mediaStorageDir == null) {
            return null;
        }

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "video_cache" + File.separator +url.hashCode() +
                "" + ".mp4.download");

        return mediaFile;
    }

    public static File getVideoThumbsDirs() {
        File mediaStorageDir = getDirectoryContainDebug(Constant.FileManager.Thumbs);
//        File file = new File(mediaStorageDir.getPath() + File.separator + name + ".ttf");
        //内部存储
//        if (m == null) {
//            return null;
//        }
//        File file = new File(m.getFilesDir(), name + ".ttf");
        return mediaStorageDir;
    }

    //得到制作幻灯片时重新绘制的图片的保存地址
    public static File getSlidePreproccessFile(int name) {
        File mediaStorageDir = getDirectoryContainDebug(Constant.FileManager.NEW_SLIDEPIC);
        File file = new File(mediaStorageDir.getPath() + File.separator + name + ".png");
        //内部存储
//        if (m == null) {
//            return null;
//        }
//        File file = new File(m.getFilesDir(), name + ".ttf");
        return file;
    }

    //得到制作幻灯片时重新绘制的图片的保存地址
    public static File getSlidePreproccessFileParant() {
        File mediaStorageDir = getDirectoryContainDebug(Constant.FileManager.NEW_SLIDEPIC);
        //内部存储
//        if (m == null) {
//            return null;
//        }
//        File file = new File(m.getFilesDir(), name + ".ttf");
        return mediaStorageDir;
    }

    //得到制作幻灯片时重新绘制的图片的保存地址
    public static File getSlideCompressFile(int name) {
        File mediaStorageDir = getDirectoryContainDebug(Constant.FileManager.COMPRESS_SLIDEPIC);
        File file = new File(mediaStorageDir.getPath() + File.separator + name + ".png");
        //内部存储
//        if (m == null) {
//            return null;
//        }
//        File file = new File(m.getFilesDir(), name + ".ttf");
        return file;
    }

    public static File getSlideCompressFileParant() {
        File mediaStorageDir = getDirectoryContainDebug(Constant.FileManager.COMPRESS_SLIDEPIC);
        //内部存储
//        if (m == null) {
//            return null;
//        }
//        File file = new File(m.getFilesDir(), name + ".ttf");
        return mediaStorageDir;
    }

    /**
     * 这里是仅供app使用，不暴露给其他App和用户
     *
     * @return
     */
    public static File getApkDownloadFile(Context m, String name) {
        //改成内部存储
        //        File file = new File(m.getFilesDir(), name + ".ttf");
        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant
                .FileManager.BQ_Separator), Constant.FileManager.APK);
        File file = new File(mediaStorageDir.getPath() + File.separator + name + ".apk");
        return file;
    }

    @NonNull
    private static File getDirectory(File externalStoragePublicDirectory, String name) {
        File mediaStorageDir = new File(externalStoragePublicDirectory, name);
        mediaStorageDir.mkdirs();
        return mediaStorageDir;
    }

    @NonNull
    private static File getDirectoryContainDebug(String gifLayer) {
        File mediaStorageDir;
        mediaStorageDir = new File(context.getCacheDir(), gifLayer);
        mediaStorageDir.mkdirs();
        return mediaStorageDir;
    }

    /***********************************************
     * 拍摄界面相关 end
     **************************************************/
    //发布的图片，分辨率大于720，进行了压缩保存
    public static File getFile4PublishCompress(int type, int tag) {
        //这里将用于分享，必须放在外部
        File mediaStorageDir = getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant
                .FileManager.BQ_Separator), Constant.FileManager.Compress);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = null;
        switch (type) {
            case 1:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + "compress" + tag + ".png");
                break;
            case 2:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + "compress" + tag + ".jpg");
                break;
        }
        return mediaFile;
    }

    @Override
    public File getWaterMarkFile(int resolution) {
        File mediaStorageDir;
//        if (BuildConfig.DEBUG){
//            mediaStorageDir= getDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + Constant
//                    .FileManager.BQ_Separator), Constant.FileManager.WaterMark);
//        }else {
        mediaStorageDir = getCacheFileParent(Constant.FileManager.Camera);
//        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "watermark_" + resolution + "_v1.png");
        return mediaFile;
    }
}
