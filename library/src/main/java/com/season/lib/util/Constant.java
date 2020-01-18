package com.season.lib.util;


public interface Constant {

    int PAGE_SIZE = 10;
    int LOW_DEVICE_MEMORY_VALUE = 150;
    long SD_SPACE_LIMIT = 1024 * 1024 * 50;//低于50m 就认为体积不够
    int TAKE_PHOTO_REQUEST = 2;// 拍照RequestCode
    /**LOW_DEVICE_MEMORY_VALUE
     * 这里的需求是八个字，自动换行，标点符号和数字算半个字符，emoji算一个字符。但是1.9.7开始取消自动换行。
     *
     * @return
     */
    int TextStyleView_MaxCharacters_PerLine = 12;// 拍照RequestCode
    String BQ_LoGer_TAG = "biaoqing?";//重要日志TAG
    String PREFS_COMMON = "pref_common";  //通用SharePreferenceP保存文件
    String LOGIN_DEVICE = "Android";
    String ALIAS_TYPE = "user_id";//推送alias别名类型设置，统一为自有id，自己设置
    String INDEX_PAGER_TYPE = "index_pager_type";
    String INDEX_PAGER_TYPE_SHOW_USER = "index_pager_type_show_user";
    String INDEX_PAGER_TYPE_GET_CHILD_CHANNEL = "index_pager_type_get_child_channel";
    String STICKER_PAGER_TYPE = "sticker_pager_type";
    String SUBJECT_DETAIL_PAGE_ID = "subject_detail_page_id";
    String SUBJECT_DETAIL_NEED_SCROLL_TO_COMMENT = "subject_detail_need_scroll_to_comment";
    String SUBJECT_DETAIL_NEED_SHOW_INPUT = "subject_detail_need_show_input";
    String CIRCLE_LIST_CHANNEL_ID = "circle_list_channel_id";
    String CIRCLE_DETAIL_TOPIC_ID = "circle_detail_topic_id";
    String CIRCLE_DETAIL_TOPIC_STATUS = "circle_detail_topic_status";
    String CIRCLE_SELECT_TYPE = "circle_select_type";
    int StickerMaxNumPerPage = 500;//贴纸单页面最多贴纸数量
    String SHOW_GRADE_DIALOG = "show_grade_dialog";

    int AREA_SELECTED_REQUEST_CODE = 0x1001;
    int AREA_SELECTED_RESULT_CODE = 0x1002;
    int MESSAGE_CODE = 0x1023;
    int FRIEND_FILTER_CODE = 0x1025;
    int FRIEND_FILTER_CODE_RESLUT = 0x1026;

    int MSG_RECEIVE_CODE = 101;

    int VIDEO_PLAY_COUNT = 2;
    int VIDEO_REPLAY_COUNT = 1;

    //设置输入状态
    int SET_TEXT_TYPING_TITLE = 101;
    int SET_VOICE_TYPING_TITLE = 102;
    int SET_TARGETID_TITLE = 103;

    float RADIO_SCREEN = 750 / 1334f;

    interface Router {
        interface App {
            String SubjectDetailActivity = "/app/SubjectDetailActivity";
            String TopicActivity = "/app/TopicActivity";
            String TopicNotHeadActivity = "/app/TopicNotHeadActivity";
            String FriendSelectActivity = "/app/FriendSelectActivity";
            String ReNameActivity = "/app/ReNameActivity";
            String WebBrowserActivity = "/app/WebBrowserActivity";
            String PerfectInfoActivity = "/app/PerfectInfoActivity";
            String CircleManageActivity = "/app/CircleManageActivity";

            String Publish = "/app/Publish";
            String PersonalActivity = "/app/PersonalActivity";
            String Main = "Main";
            String VerifyTel = "/app/VerifyTel";
            String BindPhone = "/app/BindPhone";
            String ImagePreviewActivity = "/app/ImagePreviewActivity";
            String ReportActivity = "/app/ReportActivity";
            String TopicSelectActivity = "/app/TopicSelectActivity";
        }

        interface Personal {
            String AvatarCrop = "/personal/AvatarCrop";
        }

        interface Music {
            String AudioListActivity = "/Music/AudioListActivity";
            String AlterListActivity2 = "/Music/AlterListActivity2";
        }

        interface Login {
            String Login = "/login/LoginActivity";
            String LoginModeActivity = "/login/LoginModeActivity";
            String MsgLoginActivity = "/login/MsgLoginActivity";
            String ForgetPasswordActivity = "/login/ForgetPasswordActivity";
        }


    }

    interface IndexTitles {
        String indexFollow = "关注";
        String indexNew = "最新";
        String indexRecommend = "推荐";
        String indexTopicList = "表情";
    }

    interface ShareOriginalType {//分享资源的类型
        int Video = 1;
        int Photo = 2;
        int Gif = 3;
    }

    interface PreviewTextStyleTitle {
        String Style = "样式";
        String Color = "颜色";
        String Outline = "轮廓";
        String Font = "字体";
        String Anime = "动画";
    }

    interface OriginType {
        int TYPE_IMG = 2;
        int TYPE_GIF = 1;
        int TYPE_VIDEO = 3;
    }

    interface ImageShowType {
        int TYPE_SHOW_ALL = 1;//显示链接
        int TYPE_SHOW_NO_LINK = 2;//不显示链接
        int TYPE_SHOW_ONLY_LINK = 3;//显示链接
    }

    //便于清空缓存,用于分享的文件必须放在外存
    interface FileManager {
        String BQ = "表情说说";
        String BQ_Separator = "/表情说说";
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
        String WaterMark = "WaterMark";
    }

    interface OpenParams {
        //微信
        String APPID_WEIXIN = "wxc14643be5ab20487";
        String APPKEY_WEIXIN = "89a4b6a40c51de8e48685f3360bf9ce1";
        //要去QQ互联下获取
        String APPID_QQ = "1105677233";
        String APPKEY_QQ = "DspGOsreiWxcoYtZ";
        //微博
        String APPID_WEIBO = "1291997798";
        String APPKEY_WEIBO = "3a33c468d8c540ccff0e6883297c8f09";
        String REDIRECTURL_WEIBO = "http://sns.whalecloud.com";
    }

    interface StickerTAG {
        int MINE = 0;
        int HOT = 1;
        int NEW = 2;
    }

    /**
     * 美颜默认参数,「0~1」
     * <p>
     * ios，安卓拍摄默认参数：滤镜-danyan（淡雅），美白0.3，磨皮0.5，红润0.16，瘦脸0.35，额头0.5，大眼0.2，下巴0.2
     */

    interface BeautyDefaultParams {
        //        磨皮69 美白50 红润50 亮眼69 美牙69
        float delfaut_filterLevel = 0.5f;
        float delfaut_skin_buffing = 0.69f;//磨皮
        float delfaut_skin_white = 0.50f;
        float delfaut_skin_ruddy = 0.50f;
        float delfaut_skin_brighteye = 0.69f;
        float delfaut_beauty_tooth = 0.69f;//美牙
        float delfaut_shape_bigeye = 0.4f;
        float delfaut_shape_thin_face = 0.4f;
        float delfaut_shape_forehead = 0.3f;
        float delfaut_shape_chin = 0.3f;
        float delfaut_shape_nose = 0.5f;
        float delfaut_shape_mouth = 0.4f;
        //女神-->可爱
        float delfaut_shape_bigeye_girlgod = 0.4f;
        float delfaut_shape_thin_face_girlgod = 0.4f;
        //网红-->婴儿
        float delfaut_shape_bigeye_netred = 0.4f;
        float delfaut_shape_thin_face_netred = 0.4f;
        //自然-->网红
        float delfaut_shape_bigeye_nature = 0.4f;
        float delfaut_shape_thin_face_nature = 0.4f;
    }

    //七牛云图片压缩裁剪
    interface ImageStr {
        String IndexMagicStr = "-thumb";
        String smallMagicStr = "-thumb";
        String avatarMagicStr = "";
    }

    //音乐模块
    interface musicModule {
        String hot = "推荐";
        String collection = "我的收藏";
    }

    //用户保存数据
    interface UserInfo {
        String PREFS_USER_INFO = "USER_INFO_PREFS";
        String USERID = "userId";
        String USERTOKEN = "userToken";
        String USERAVATAR = "userAvatar";
        String NICKNAME = "nickName";
        String SUPERTYPE = "supertype";
        String LOGIN_TYPE = "loginType";
        String LOGIN_TOKEN = "loginToken";
    }

    interface bottomOperateTag {
        int INIT_TAG = -1;
        int OVERLAY = 0;
        int SHARE = 1;
        int TEXTSTYLE = 2;
        int VOICE = 3;
        int STICKER = 4;
//        int TEXTSTYLENEW = 5;
    }

    //TODO 这样不好维护，建议有空换成对象来维护
    interface User {
        String SP_USER_INFO = "SP_USER_INFO";
        String UID = "uid";
        String TOKEN = "token";
        String LOGIN_TYPE = "loginType";
        String LOGIN_TOKEN = "loginToken";

        String AVATAR = "avatar";
        String NICKNAME = "nickname";
        String SEX = "sex";
        String SUMMARY = "summary";
        String BIRTHDAY = "birthday";
        String ADDRESS = "address";

        String TAG = "tag";

        String WORK_NUM = "worksNum";
        String FORWARD_NUM = "forwardNum";
        String FOLLOW_NUM = "followNum";
        String FANS_NUM = "fansNum";

        String WORK_LIKE_NUM = "work_like_num";
        String USER_TYPE = "user_type";

        String VERIFIEDREASON = "verifiedReason";

        String LEVEL = "level";
        String LEVEL_EXP = "level_exp";

        String IS_PHONE = "is_phone";

        String COVER = "cover";

        String PENDANT = "pendant";

        String AVATAR_AUDIT = "avatar_audit";

        String MY_LIKE_WORKS_NUM = "my_like_works_num";

        String INVITE_CODE = "invite_code";

        String WECHAT_ID = "wechat_id";
    }

    //登录类型
    interface LoginType {
        String QQ = "qq";
        String WEIXIN = "weixin";
        String SINA = "sina";
        String PHONE = "phone";
    }

    interface MakeFace {

        int clearView = -1;//清除container
        int makeText = 0;// 文字
        int makeFont = 1;// 字体
        int showPickView = 2;// 显示 色盘//改变字体颜色
        int dismissPickView = 3;// 关闭 色盘
        int sendTextToEdit = 4;// 发送文字到 文本编辑到的editext
        int sendTextToStickView = 5;// 发送文字到 stickView
        int makeFontColor = 6;// 字体颜色
        int makeFontPickerColor = 7;//色板字体颜色
        int makeTextBoldWith = 8;// 字体粗细
        int makeTextAlpha = 9;//字体透明变化

        int makeTextStraw = 10;//吸色管
        int makeTextBuddle = 11;//气泡
        int makeTextStrokeWidth = 12;//描边粗细
        int makeTextStrokeAlphal = 13;//描边透明度
        int makeTextStrokeColor = 14;//描边颜色
        int showPickView_OutLine = 15;// 显示 色盘// 改变 轮廓颜色
        int showPickView_stroke_font = 16;// 显示 色盘// 改变 轮廓颜色
        // int dismissSourceShopView = 17;//隐藏历史或横向素材 view
        int sourceUrl = 18;//选择素材的url
        int sourceUrlHistory = -18;//选择素材的url
        int copyView = 19;//复制图层
        int down = 20;//向下一层
        int up = 21;//向上一层
        int brush = 23;//点击了画笔
        int brush_finish = -23;//画笔展开的时候点击，完成绘制
        int erase = 24;//擦除
        int painwith = 25;// 画笔粗细
        int addLoaclImageHistory = -27;// 添加本地图片到画布
        int addLoaclImageJson = -26;// 添加本地图片到画布
        int dismissCropView = 27;// 移除 裁剪view
        int dismissFilterView = 28;// 移除 滤镜view
        int filter = 29;// 滤镜 调节
        int chooseFilter = 30;//点击选择滤镜
        int showCropMask = 31;//显示裁剪蒙板
        int paintColor = 33;//涂鸦画笔颜色
        int paintPickerColor = 34;//画笔色板选择的颜色
        int paintStyle = 35;//图片画笔风格
        int paintMasksTyle = 36;//荧光笔风格
        int paintAlpha = 37;//涂鸦画笔的透明度
        int Mosaic = 38;//添加马赛克图层
        int painGetColor = 40;// 画笔粗细
        int imageMaterial = 41;
        int imageMaterialHistory = 42;//选择素材的url
        int imageMaterialLocal = 43;
        int textAnimation = 44;// 字体粗细
//        int textColorSize = 45;// 同步颜色百分比（内描边）
//        int textStrokeSize = 46;// 同步描边百分比（内描边）
    }

    interface Info {
        int SEX = 00;
        int NICKNAME = 01;
        int BIRTHDAY = 02;
        int LOCATION = 03;
        int SUMMARY = 04;
    }

    interface TEXT {
        int TEXTSIZE_PX = 100;
        int TEXTSIZE = AutoUtils.getPercentWidthSize(TEXTSIZE_PX);
        float BIGTEXTSIZE_TIMES = 1.5f;//字体小于4做一个放大。在预览界面。
        int PAINTWIDTH = AutoUtils.getPercentWidthSize(TEXTSIZE_PX / 8) + 1;
        //        int STROKEWIDth = AutoUtils.getPercentWidthSize(TEXTSIZE_PX / 8) + 1;
        int STROKEWIDth = AutoUtils.getPercentWidthSize(8);
        int TEXT_PREVENT_CUT = AutoUtils.getPercentWidthSize(16);//防止字体被裁切
        int PADDING = AutoUtils.getPercentWidthSize(20);
    }

    interface TUYA {
        int UNSelectFistRowColor = -1;
        int UNSelectALl = -2;
        int STROKEWIDth = 4;
        int PAINTWIDTH = 2;
        String ADD_TuyaBitmap = "ADD_TuyaBitmap";
        String ADD_MosaicBitmap = "ADD_MosaicBitmap";
        String TUYALAYER_FILENAME = "TuyaLayer";
    }


    interface APP {
        String APPNAME = "表情说说";
        int GIFHEIGHT = 100;
    }

    interface HISTORY {
        int STICKER_HISTORY = 1;
        int TUYA_HISTORY = 2;
        int CAMERA_SOURCE_HISTORY = 3;
        int IMAGE_HISTORY = 4;
    }

    interface STICKER_ACTION_RECORD {
        int ACTION_NULL = 0;
        int ACTION_ADD_VIEW = 1;
        int ACTION_MOVE_VIEW = 2;
        int ACTION_ROTATE_VIEW = 3;
        int ACTION_DELETE_VIEW = 4;
        int ACTION_SCALEX_VIEW = 5;
        int ACTION_SCALEY_VIEW = 6;
    }

    interface Diy {
        interface Text {
            int AlphaMaxProgress = 255;
        }
    }

    interface AudioRecognize {
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        int hasPunctuation = 0;
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        int maxStopSpeakTime = 3000;
    }

    interface HandlerWhatTAG {
        interface Camera {
            int RecordDone = 100;
            int RecordDoneNext = 200;
            int RecordLongPress = 300;
            int StartRecord = 400;
            int RecordDoneWaitXunfeiLanguageRecognize = 500;
            int NewAudioRecognize = 600;
        }

        interface Preview {
            int videoStart = 100;
        }
    }

    interface System {
        interface Mode {
            String SmartisanR1 = "DE106";
        }
    }

    interface GlobalSettings {
        boolean isAlterWithoutLayer = true;//改图不使用图层。底图是成品
        boolean isXunfeiRecordWhenVideoRecording = false;//讯飞是否在拍摄的时候去录制；如果不是，就是合成完整视频的时候取音频，再用SDK识别。
        boolean isShowShareIconAfterPublishSuccess = false;
        boolean needOverLayer = false;//制作过程中是否打水印
    }

    /**
     * 关于相机拍摄参数的调整，和生成作品的参数调整。
     * 发布的时候从相册提取，静态图最小边要小于等于720
     * 相册提取gif，长和宽都必要小于360
     */
    interface Camerasettings {
        double videoRateGifSpeed = 2d;
        int recordHandlerDelayTime = 20;
        int videoHandlerSpace = 20;
        int audioSampleSize = 16000;//44100
        int audioChannelCount = 1;//defalut:1
        int MP4DelayTime4FirstVideoWhenFollowShot = 0;//TODO
        int MusicMoreTime = 0;
        int FRAME_RATE = 30;
        int MIN_DURATION = 3000;
        String MIN_DURATION_TIPS = "视频时长不能小于" + (Camerasettings.MIN_DURATION / 1000) + "秒哦";
        long VIDEOMODE_DURATION = 10000;//自由拍摄
        long UNIT_TIME = VIDEOMODE_DURATION;
        //生成视频每秒帧数 30
        //webp质量参数 1
        //动态图每秒帧数 7
        //拍照分辨率 720
        int PHOTO_RESOLUTION = 480;
        int PHOTO_RESOLUTION_WECHAT_SHARE = 300;
        int PHOTO_MAX_RESOLUTION = 720;
        //视频分辨率 480
        int VIDEO_RESOLUTION = 720;
        int DIY_STICKER_BASE_SIZE = 480;//贴纸缩放比例尺寸
        int IDEAL_VIDEO_RESOLUTION = 720;//视频分辨率
        //分享表情的分辨率  300
        int SHARE_WECHAT_GIF_RESOLUTION = 240;
        int IDEAL_GIF_RESOLUTION = 300;//320
        int SHARE_RESOLUTION_PICTURE = 360;
        //发布到平台的分辨率 720
        int APPTARGET_RESOLUTION = 720;
        int VIDEO_CROP_THUMB_MIN = 8;
        int GIF_FAMES_NUM_SHARE = 8;
        float GIF_FAMES_NUM_SHARE_4 = 6;
        float GIF_FAMES_NUM_SHARE_5 = 4.5f;
        float GIF_FAMES_NUM_PUBLISH = 8;
        int keyint = 10;//keyint 每隔多少帧 生产一个关键帧
        int ThumbsResolution = 40;//视频缩率图片的分辨率
        int NormalVideoBitRate = 3072000;
        String keepVideoToLocalTips = "已保存至DCIM/Camera文件夹";
        double SMALL_VIDEO_DURATION_THRESHOLD_S = 10.5;
    }

    /**
     * 分享GIF：
     * 固定每秒5帧
     * 加速只要勾选即有效  不分几秒
     * 3秒及以下  300x300
     * 3秒以上  240x240
     * 长按分享原图到微信 保存到本地相册
     * 统一不加速 每秒10帧 300x300
     * -----------
     * 制作逻辑不改动：
     * 合成gif那块  目前还是保持每秒8帧
     */
    interface ShareSettings {
        int Fps_QQ_Wechat = 5;//全平台
        int Resolution_DurationBelow3Second = 300;
        int Resolution_DurationOver3Second = 240;
        int Fps_HD_WechatOrKeepLocal = 10;
        int Resolution_HD_WechatOrKeepLocal = 300;
        int ShareWindowAutoCloseTime = 2500;
    }

    interface DiySettings {
        int textSingleLineMaxTextNum = 10;
    }

    interface GETFILE_TYPE {
        int SHARE_TEXTLAYER = 1;
        int SHARE_GIF = 2;
        int SHARE_MP4 = 3;
    }

    interface UmengOnEvent {
        String Camera_Re_Video = "Camera_Re_Video";//录像
        String Camera_Re_Photo = "Camera_Re_Photo";//拍照
        String Camera_O_Post = "Camera_O_Post";//使用后置摄像头
        String Camera_O_Front = "Camera_O_Front";//使用前置摄像头
        String BQSPublish_Photo = "BQSPublish_Photo";//相册
        String BQSPublish_Make = "BQSPublish_Make";//制作
        String BQSPublish_Camera = "BQSPublish_Camera";//拍摄
        String BQSPublish_Btn = "BQSPublish_Btn";//发布
    }

    interface UpUploadFolderPath {
        String STUFF = "stuff/";
        String WORK = "work/";//作品 单号固定000 多作品：000～008
        String WORK_COMMENT = "upfile/";//作品 单号固定000 多作品：000～008
        String WORK_DEBUG = "test/work/";//作品 单号固定000 多作品：000～008
        String WORK_COMMENT_DEBUG = "test/upfile/";//评论   单号固定000 多作品：000～008
        String BG_VIDEO = "video/";//底图是视频 固定000 3个0
        String BG_VIDEO_DEBUG = "test/video/";
        String BG_STATIC_PIC = STUFF;//底图是图片 固定0000  4个0
        String BG_STATIC_PIC_DEBUG = "test/" + STUFF;
        String STICKER_CROP = STUFF;//裁剪的图片 000 叠加 ，从0开始
        String STICKER_TUYA = STUFF;//涂鸦图片   000 叠加  ，从0开始
        String STICKER_DEBUG = "test/" + STUFF;//作品 单号固定000 多作品：000～008

        String VERIFY = "verify/";//认证
        String VERIFY_DEBUG = "test/verify/";//认证

        String AVATAR = "avatar/";//头像
        String AVATAR_DEBUG = "test/avatar/";//头像

        String COVER = "user/cover/";//个性封面
        String COVER_DEBUG = "test/user/cover/";//个性封面
    }

    interface FFmpegActionType {
        int CROP_GET_POSITIVE_URL = 1;
        int CROP_VIDEO_DURATION = 2;
        int OPERATE_VIDEO_PRODUCE_GIF = 3;//生产gif
    }

    interface Matisse_RequestCode {
        String Matisse_RequestCode = "Matisse_RequestCode";
        int fromCamera = 11;
        int fromMainPlus = 22;//可能是首页，可能是有头部的专题页面，也可能是没有头部的专题页面
        int fromPublish = 33;
        int fromUserPortrait = 44;
        int fromDiy = 55;
        int fromRegisterPortrait = 66;
        int fromDiyCut = 77;
        int fromCover = 88;
    }

    interface PublishFromTag {
        String fromDraft = "fromDraft";
        String fromCamera = "fromCamera";
        String fromMainPlus = "fromMainPlus";
    }

    interface CAMERA_STATE_SAVE {
        String isOPEN_LANGUAGE_RECOGNIZE = "isOPEN_LANGUAGE_RECOGNIZE";
        String isOPEN_BEAUTY_MODE = "isOPEN_BEAUTY_MODE";
        String isBackForward = "isBackForward";
        String isPhotoOrVideo = "isPhotoOrVideo";
    }

    interface ACTIVITY_REQUEST_CODE {
        int PUBLISH_PHOTOALBUM = 1;
    }

    interface RequestCode {
        interface ModuleMusic {
            int MusicList = 0;
            int SearchMusic = 1;
        }
    }

    interface IndexVideoShareType {
        int SHARE_TYPE_REPLAY = 0;
        int SHARE_TYPE_GIF = 1;
        int SHARE_TYPE_VIDEO = 2;
        int SHARE_TYPE_WEB = 3;
        int SHARE_TYPE_MORE = 4;
    }

    interface IntentTag {
        interface Report {
            String valId = "valId";
            String type = "type";
        }


        interface Mine {
            String Personal_UserId = "userId";
            String AvatarCrop_URI = "uri";
            String AvatarCrop_URL = "url";
        }

        interface SubjectDeatail {
            String id = "id";
            String idList = "idList";
            String params = "params";
            String needScrollToComment = "needScrollToComment";
        }

        interface ImagePreview {
            String IMAGE_INFO = "IMAGE_INFO";
            String CURRENT_ITEM = "CURRENT_ITEM";
        }

        interface Camera {
            String url = "url";
            String isFromAlbum = "isFromAlbum";
            //            String camera_tag="camera_tag";
            String isVideo = "isVideo";
            String isGif = "isGif";
            String text = "text";
            String isSlide = "isSlide";
            String musicBean = "musicBean";

            interface Camera_Purpose//拍照的目的
            {
                String CAMERA_PURPOSE_TAG = "CAMERA_PURPOSE_TAG";
                int fromMainPlus = 1;//可能是首页，可能是有头部的专题页面，也可能是没有头部的专题页面
                int fromDiy = 2;
            }
        }

        interface Router {
        }

        interface Album {
            String MatisseActivity_EXTRA_RESULT_SELECTION = "extra_result_selection";
            String MatisseActivity_EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";
            String AlbumPreview_Current_Position = "CURR_ITEM";
            String AlbumPreview_Url_List = "MUTIL_URLS";
        }

        interface Diy {
            String MaterialDetailActivity_BEAN = "data";
            String CropActivity_File = "file";
            String Alter_fullVideo = "fullVideo";
            String Alter_layerInfo = "layerInfo";
            String Alter_coverurl = "coverurl";
            String Alter_originalId = "originalId";
            String Alter_changeId = "changeId";
            String DIY_isPrivate = "DIY_isPrivate";
            String DIY_notRecommend = "DIY_notRecommend";
            String DIY_DraftId = "DIY_DraftId";
            String PATH = "PATH_A";
            String GIFPATH = "GIFPATH";
            String isAlter = "isAlter";
        }

        String PUBLISH_TOPIC_DATA = "PUBLISH_TOPIC_DATA";
        String PUBLISH_ATE_DATA = "PUBLISH_ATE_DATA";
        String PUBLISH_RANGE = "PUBLISH_RANGE";
        //        String PUBLISH_ISALTER = "PUBLISH_ISALTER";//是否允许改图
        String PUBLISH_RES = "PUBLISH_RES";
        //        String DIY_isPrivate = "DIY_isPrivate";
//        String DIY_notRecommend = "DIY_notRecommend";
//        String DIY_DraftId = "DIY_DraftId";
        //        String DIY_DraftText = "DIY_DraftText";
        //        String AudioId = "AudioId";
        String AudioBean = "AudioBean";
        String AudioBeanFromCamera = "AudioBeanFromCamera";
        String AudioPosition = "AudioPosition";
        String followShotVideo = "followShotVideo";
        String followShotText = "followShotText";
        String followShotgif = "followShotgif";
        String followShotAudioId = "followShotAudioId";
        String followShot = "followShotResStart";
        String followId = "followId";
        String OriginId = "OriginId";
        String SubjectId = "SubjectId";
        String alterFollowTag = "alterFollowTag";
        String musicClassifyName = "musicClassifyName";
        String musicClassifyId = "musicClassifyId";
        String musicisPreview = "musicisPreview";
        String musicisPreviewVideoTotalTime = "videoTotalTime";
        //        String faceId = "faceId";
//        String AudioCover = "AudioCover";
//        String AudioUrl = "AudioUrl";
        String fromSubject = "fromSubject";
        String publishWorkisVideo = "publishWorkisVideo";
        String musicRes = "musicRes";
        String OperateCircleType = "operateCircleType";
    }

    /**
     * 1,改图；2，跟拍;3,斗图
     */
    interface relateType {
        int none = 0;
        int Alter = 1;
        int Follow = 2;
    }

    interface PUBLISH_RANGE {
        int PUBLIC = 1;
        int FANS = 2;
        int PRIVATE = 3;
    }

    interface httpParams {
        interface status {
            int news = 0;
            int hot = 1;
        }

        interface subject_related {
            int alterlist = 1;
            int followlist = 2;
            int all = 3;
        }
    }

    interface PreviewType {
        String FROM_WHERE = "FROM_WHERE";
        int FROM_CAMERA = 1;
        int FROM_PUBLISH = 2;
        int FROM_CANVAS = 3;
        int PreviewWork = 4;
    }

    /**
     * 图片展示模式
     */
    interface PhotoShowMode {
        String HIGH_EFFECT = "high_effect";
        String LOW_EFFECT = "low_effect";
    }

    /**
     * 图片展示模式
     */
    interface WaterMarkType {
        //旧版本
        String WATER_MARK_LOGO = "water_mark_logo";
        String WATER_MARK_LOGO_NAME = "water_mark_logo_name";
        String WATER_MARK_NAME = "water_mark_name";

        //无水印，水印第一个
        String WATER_MARK_NONE = "water_mark_none";
        //水印第二个，用户名
        String WATER_MARK_LOGO_USERNAME = "water_mark_logo_username";

        //1.6版本之后新增
        //水印第三个，带眼睛
        String WATER_MARK_NAME_EYES = "water_mark_name_eyes";
        //表情说说，水印第四个
        String WATER_MARK_APP_NAME = "water_mark_app_name";
        //方形，水印第五个
        String WATER_MARK_SQUARE = "water_mark_square";
        //横幅，水印第六个
        String WATER_MARK_NAME_BANNER = "water_mark_name_banner";

        //刷子，水印第七个
        String WATER_MARK_NAME_BRUSH = "water_mark_name_brush";
        //相机，水印第八个
        String WATER_MARK_NAME_CAMERA = "water_mark_name_camera";
        //猫耳朵，水印第九个
        String WATER_MARK_NAME_CAT = "water_mark_name_cat";
    }

    /**
     * 区分预览界面保存到本地，分享，和准备发布的动作
     */

    interface CameraPreviewOperateType {
        int Share = 1;
        int SaveLocal = 2;
        int Publish = 3;
        int Diy = 4;//预览页面进入制作
        int Diy_ADD_CANVAS = 5;//制作页面添加相册
        int SaveLocal_Video = 6;
    }

    interface ShareOperateType {
        int WEIXIN_YUANTU = 1;
        int NORMAL_SHARE = 2;
        int SAVE_LOCAL = 3;
        int SYSTEM_SHARE = 4;
        int SAVE_LOCAL_VIDEO = 5;
        int WEIBO = 6;
        int WEIXIN_CIRCLE = 7;
        int QZONE = 8;
    }

    /**
     * 拍摄保存的文件名
     * * 一个视频，normalvideo.mp4
     * 倒叙视频，reverse.mp4
     * 乒乓视频，pingpang.mp4
     * 裁剪的视频，crop.mp4
     * 更改分辨率的视频，resolution.mp4
     * 合成的视频，merge.mp4
     * 截取的水印层，overlay.png
     * 更改分辨率的水印层 overlay4video.png
     * gif图层，final.gif
     * <p>
     * Photo:
     * (不像视频那么复杂，我们区块截屏，然后再调整分辨率就好)
     * 一张图片，normalphoto.png
     * 合成的照片 mergephoto.png
     * 调整分辨率的照片 finalPhoto.png
     */
    interface CameraFileName {
        interface CameraFileNameTag {
            int NORMAL_VIDEO = 1;
            int REVERSE = 2;
            int PINGPANG = 3;
            int CROP_VIDEO_SIZE = 4;
            int RESOLUTION_VIDEO = 5;
            int MERGE_VIDEO = 6;
            int OVERLAY = 7;
            int OVERLAY4VIDEO = 8;
            int FINALGIF = 9;//最后生成的gif和照片，不能放在内部存储，因为发布到平台后，有一个分享按钮，这时候我们是直接对原文件进行分享，必须在外存。
            // （可能做压缩，所以压缩文件也必须放在外存）
            int NORMAL_PHOTO = 10;
            int MERGE_PHOTO = 11;
            int FINALPHOTO = 12;//最后生成的gif和照片，不能放在内部存储，
            int CROP_VIDEO_DURATION = 13;
            int SPEED_VIDEO = 14;
            int GIF_COLOUR_BOARD = 15;
            int IMG_WATERMARK_IOS_ORIGINAL = 16;
            int IMG_WATERMARK_IOS_ADJUST = 17;
            int NORMAL_VIDEO_PLUS_KEY_FRAME = 18;
            int POSITIVE_TS = 19;
            int REVERSE_TS = 20;
            int TEST_BITMAP = 21;
            int TIME_VIDEO = 22;
            int TIME_VIDEO_CONTACT_TEXT = 23;
            int OVERLAY_VIDEO = 24;
            int LAYER_GIF = 25;
            int MERGE_VIDEO4GIF = 26;
            int DRAFT_VIDEO = 27;
            int DRAFT_VIDEO_ASSET = 28;
            int MUX_AV_VIDEO = 29;
            int VIDEO_THUBM = 30;
            int CameraAudio = 31;
            int CameraAdjustDuration = 32;
            int AudioFromShotVideo = 33;
        }

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
        String CameraAudio = "CameraAudio.mp4";
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

    interface FileSuffixWithPoint {
        String JPG = ".jpg";
        String PNG = ".png";
        //        String GIF = ".gif";
        String MP4 = ".mp4";
        String WAV = ".wav";
        String MP3 = ".mp3";
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

    interface diyOperateType {
        int text = 0;
        int layer = 1;
        int history = 2;
    }

    interface ToolViewsType {
        //本地素材 绘图 要上传图片
        int ButtonPositionTypeTop = 0;
        int ButtonPositionTypeLeft = 1;
        int PositionTypeBottom = 2;
        int ButtonPositionTypeRight = 3;
    }

    interface HistoryWeiXinLogin {
        String PREFS_HISTORY_WEIXIN_LOGIN = "prefs_history_weixin_login";
        String ACCESS_TOKEN = "access_token";
        String AVATAR = "avatar";
        String NICKNAME = "nickname";
        String TOKEN = "token";
        String LOGIN_TIME = "login_time";
        String LOGIN_TYPE = "login_type";

        String USER_ID = "user_id";
    }

    interface HistoryWeiBoLogin {
        String PREFS_HISTORY_WEIBO_LOGIN = "prefs_history_weibo_login";
        String ACCESS_TOKEN = "access_token";
        String AVATAR = "avatar";
        String NICKNAME = "nickname";
        String TOKEN = "token";
        String LOGIN_TIME = "login_time";
        String LOGIN_TYPE = "login_type";

        String USER_ID = "user_id";
    }

    interface HistoryQQLogin {
        String PREFS_HISTORY_QQ_LOGIN = "prefs_history_qq_login";
        String ACCESS_TOKEN = "access_token";
        String AVATAR = "avatar";
        String NICKNAME = "nickname";
        String TOKEN = "token";
        String LOGIN_TIME = "login_time";
        String LOGIN_TYPE = "login_type";

        String USER_ID = "user_id";
    }

    interface HistoryPhoneLogin {
        String PREFS_HISTORY_PHONE_LOGIN = "prefs_history_phone_login";
        String ACCESS_TOKEN = "access_token";
        String AVATAR = "avatar";
        String NICKNAME = "nickname";
        String TOKEN = "token";
        String LOGIN_TIME = "login_time";
        String LOGIN_TYPE = "login_type";
        String PHONE = "phone";

        String USER_ID = "user_id";
    }

    interface PermissionCode {
        int UseCamera = 100;
        int UseAlbums = 200;
        int RequestPermissionOnStart = 300;
        int UseContact = 400;
        int UseCameraAvatar = 500;
        int LocationFriend = 600;
    }

    interface UmengManager {
        boolean isrelease = true;//是否调用 UMShareAPI.TopicGroupAPIAOP(this).release();
        boolean isrelease4SubjectPreview = false;//表情详情和表情预览的界面，两处分享回收友盟资源，异常可能性大
    }

    interface IndexShowMode {
        String STAGGERED = "staggered";
        String LIST = "list";
    }

    interface CircleShowMode {
        String STAGGERED = "staggered";
        String LIST = "list";
    }

    interface TopicShowMode {
        String STAGGERED = "staggered";
        String LIST = "list";
    }

    interface IndexNewType {
        String ALL = "全部";
        String HAS_VIDEO = "视频";
        String NONE_VIDEO = "GIF动图";
        String STATIC = "静图";
    }

    interface JumpToSubjectType {
        String INDEX_NEW = "index_new";
        String INDEX_NEW_4_USER = "index_new_4_user";
        String INDEX_HOT = "index_hot";
        String INDEX_FOLLOW = "index_follow";
        String ALL_CHANNEL = "all_channel";
        String TOPIC = "topic";
        String MY_WORK = "my_work";
        String OTHER_WORK = "other_work";
        String MY_LIKE_SUBJECT = "my_like_subject";
        String MY_LIKE_ALBUM = "my_like_album";
        String OTHER_LIKE = "other_like";
        String SEARCH = "search";
        String ALTER_LIST = "alter_list";
        String CIRCLE_DETAIL_LIST = "circle_detail_list";
        String CIRCLE_DETAIL_FEED = "circle_detail_feed";
        String INDEX_REC_WORK = "index_rec_work";
        String INDEX_CITY = "index_city";
        String INDEX_REC_LIST = "index_rec_list";
    }

    interface SharePlatform {
        String WECHAT = "wechat";
        String QQ = "qq";
        String QZONE = "qzone";
        String WECHAT_CIRCLE = "wechat_circle";
        String WEIBO = "weibo";
    }

    interface WebBrowser {
        String WEB_URL = "web_url";
        String SHARE_NAME = "share_name";
        String SHARE_CONTENT = "share_content";
        String SHARE_THUMB = "share_thumb";
        String SHOW_SHARE = "show_share";
        String SHARE_URL = "share_url";
    }

    interface CircleList {
        String CIRCLE_TYPE = "circle_type";
        String BANNER_TYPE = "banner_type";
    }

    interface CommentInputShowType {
        String EMOJI = "emoji";
        String GIF = "gif";
    }

    interface CommentIntentKey {
        String ID = "id";
        String SHOW_TYPE = "show_type";
        String BE_REPLY_ID = "be_reply_id";
        String BE_REPLY_COMMENTID = "be_reply_commentid";
        String NAME = "name";
        String BE_REPLY = "be_reply";
        String HAS_COMMENT = "has_comment";
    }

    interface CacheSize {
        long picCacheSize = 250 * 1024 * 1024;
        long videoCacheSize = 200 * 1024 * 1024;
    }

    interface ShareType {
        int LINK_COPY = 1;//复制链接
        int LINK_WEIBO = 2;//微博链接
        int LINK_WEIXIN_CIRCLE = 3;//微信朋友圈链接
        int LINK_WEIXIN = 4;//微信链接
        int LINK_QZONE = 5;//朋友圈链接
        int LINK_QQ = 6;//QQ
        int QQ = 7;//QQ
        int QZONE = 8;//朋友圈
        int XITONG_FENXIONG = 9;//系统分享
        int WEXIN = 10;//微信
        int WEXIN_CIRCLE = 11;//微信朋友圈
        int WEIBO = 12;//微博
        int SAVE = 13;//保存
        int WEXIN_YUAN_TU = 14;//微信原圖
        int LINK_XITONG_FENXIAN = 15;//系統分享鏈接
        int LINK_CREATE_GIF = 16;//系統分享鏈接
    }

    interface  ReportType{
        /**
         * 作品
         */
        String REPORT_WORKS ="1";
        /**
         * 评论
         */
        String REPORT_COMMENT  = "2";
        /**
         * 评论用户
         */
        String REPORT_USER = "3";
    }
}
