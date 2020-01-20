package com.season.lib.util;


public interface Constant {

    int PHOTO_RESOLUTION = 480;
    int PHOTO_RESOLUTION_WECHAT_SHARE = 300;
    //视频分辨率 480
    int DIY_STICKER_BASE_SIZE = 480;//贴纸缩放比例尺寸
    int IDEAL_VIDEO_RESOLUTION = 720;//视频分辨率
    //分享表情的分辨率  300
    int SHARE_WECHAT_GIF_RESOLUTION = 240;
    int IDEAL_GIF_RESOLUTION = 300;//320

    interface ShareOriginalType {//分享资源的类型
        int Photo = 2;
        int Gif = 3;
    }

    //便于清空缓存,用于分享的文件必须放在外存
    interface FileManager {
        String BQ_Separator = "/ps";
        String Bitmap = "Bitmap";
        String APK = "Apk";
        String Share = "Share";//发布的时候放在外部，将可能用于分享
        String Camera = "Camera";//发布的时候放在内部
        String Thumbs = "Thumbs";//发布的时候放在内部
        String CAMERA_MUSIC = "CAMERA_MUSIC";//发布的时候放在内部
        String FollotShotVideo = "FollotShotVideo";//发布的时候放在内部
        String NEW_SLIDEPIC = "New_slidepic";//发布的时候放在内部
        String COMPRESS_SLIDEPIC = "Compress_slidepic";//发布的时候放在内部
        String Compress = "Compress";//发布的时候放在外部，将可能用于分享
        String DIY = "DIY";//部分资源存在内存的DIY下；最终生成的作品在外存的DIY下
        String CAMERA_BITMAP = "VideoBitmap";//视频生成的bitmap文件缓存目录
        //以下都是DIY的二级目录
        String Layer = "Layer";
        String Download = "Download";
        String Crop = "Crop";
        String Material = "Material";
        String Font = "Font";
        String Background = "Background";
        String GIF_LAYER = "GIF_LAYER";
        String STICKER = "Sticker";
    }

    interface CameraFileName {


        String NORMAL_VIDEO = "normalvideo.mp4";
        String NORMAL_VIDEO_PLUS_KEY_FRAME = "normalvideo_keyframe.mp4";
        String REVERSE = "reverse.mp4";
        String PINGPANG = "pingpang.mp4";
        String CROP_VIDEO_SIZE = "cropsize.mp4";
        String RESOLUTION_VIDEO = "resolution.mp4";
        String MERGE_VIDEO = "merge.mp4";
        String OVERLAY = "overlay.png";
        String OVERLAY4VIDEO = "overlay4video.png";//图片，带有水印的图片
        String FINALGIF = "final.gif";
        String NORMAL_PHOTO = "normalphoto.png";
        String MERGE_PHOTO = "mergephoto.png";
        String FINALPHOTO = "finalphoto.png";
        String CROP_VIDEO_DURATION = "cropduration.mp4";
        String SPEED_VIDEO = "speed_video.mp4";
        String GIF_COLOUR_BOARD = "gif_colour_board.png";
        String IMG_WATERMARK_IOS_ORIGINAL = "img_watermark_ios_original.png";
        String IMG_WATERMARK_IOS_ADJUST = "img_watermark_ios_adjust.png";
        String POSITIVE_TS = "postive.ts";
        String REVERSE_TS = "reverse.ts";
        String TEST_BITMAP = "TEST_BITMAP.png";
        String TIME_VIDEO = "time_video.mp4";
        String TIME_VIDEO_CONTACT_TEXT = "time_video_contact_text.txt";
        String OVERLAY_VIDEO = "overlay_video.mp4";//打上水印的视频
        String LAYER_GIF = "layer.gif";//图层gif
        String MERGE_VIDEO4GIF = "merge4gif.mp4";
        String DRAFT_VIDEO = "draft_video.mp4";
        String DRAFT_VIDEO_ASSET = "draft_video_asset.mp4";
        String MUX_AV_VIDEO = "robotaudio_video.mp4";
        String VIDEO_THUMB = "VIDEO_THUMB.jpg";
        String CameraAdjustDuration = "CameraAdjustDuration.mp4";
        String AudioFromShotVideo = "AudioFromShotVideo.wav";
    }

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
     * 上传服务器的资源的分类
     */
    interface contentViewType {
        //本地素材 绘图 要上传图片
        int ContentViewTypeImage = 0; //网络素材
        int ContentViewTypeLocaImage = 1;//本地素材
        int ContentViewTypeTextbox = 2;//文字
        int ContentViewTypeDraw = 3;//涂鸦，绘图
    }

}
