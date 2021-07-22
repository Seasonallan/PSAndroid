# 区块链&PS图层&插件化&阅读器
+ [测试APK下载](https://github.com/Seasonallan/PSAndroid/blob/master/apk/season.apk)


## 区块链

+ 私钥生成流程    
输入助记词：original desert extra joy cycle install crystal ocean around wing dirt unlock    
    + 生成seed：    
[-121, 30, 66, -15, -57, 122, -84, 12, 97, -57, 90, 18, -26, -97, -36, -125, 3, 58, -87, -16, 40, -111, 1, -6, 5, 27, 110, 49, -36, 43, -57, -90, 60, -3, 79, -44, 117, 27, 19, 2, -12, -124, -44, -56, -47, -28, -73, -122, 16, -79, 20, 87, 98, 67, -125, 91, -56, 76, -111, -12, -119, 15, 2, 0]    
    + 编码seed： 以 "Bitcoin seed".getBytes()为key对seed进行HmacSHA512编码    
[69, -60, 25, 85, -17, 58, -126, 34, -54, 94, 28, -16, 72, 42, -108, 102, -56, 71, -92, -93, 89, 47, 25, -28, -1, -78, 116, 120, 7, -7, -108, -107, 11, -16, 98, -54, -65, -17, 100, 120, -113, -99, -67, -55, 82, 47, -119, -27, 122, 32, 54, -100, -102, 32, -15, 46, 8, 118, -94, 82, -32, -123, -13, -40]    
    + 生成ExtendedKey：使用前32位为priv，后32位为chainCode    
    + 生成RawPrivateKey：priv 32位，不足进行补位，多余取后面，对priv进行5层变化    
m/purpse/coin_type/account/change/address_index    
        + purpse | 0x80000000     以chainCode为key对priv+4位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + coinType | 0x80000000  以chainCode为key对priv+4位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + account | 0x80000000   以chainCode为key对priv+4位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + change   以chainCode为key对priv+5位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
        + address_index    以chainCode为key对priv+5位进行HmacSHA512编码，前32位结果加上原priv数据为新priv，后32位为新chainCode。    
[-117, 21, 40, -13, -12, 100, 13, 126, -108, 125, -69, -69, -50, 4, 64, -102, -72, 58, 23, -115, 117, -13, 99, -18, 83, 50, 105, 102, 63, -113, 2, -21]    
    + 生成PrivateKey：    
        + 添加版本标记0x80（主网）或0xef（测试网），不同的币种有不同的版本标记，如LTC是0xb0    
        + 添加0x01 + 4位校验码到尾部    
        + BASE58编码    
L1t4ygXx2x7KBg846aMddmr4KYgnpb2XXAhEmHGqAFTVGvoarBzK    

+ 公钥生成流程    
    + 对PrivateKey进行椭圆曲线算法生成pubComp数组    
[2, 35, -97, -111, 98, -102, 34, 85, -105, -11, 58, 17, -125, 33, 119, 98, -44, -13, -127, 121, -86, 105, -65, -95, -2, 63, -118, 86, -119, 37, -33, 113, -24]    
    + 对pub数组进行 Hex编码    
02239f91629a225597f53a1183217762d4f38179aa69bfa1fe3f8a568925df71e8    

+ 地址生成流程    
    + 对pub数组进行SHA256_RIPEMD160加密    
    + 添加版本前缀，不同的币种有不同的版本前缀   
    + SHA256签名两次    
    + 将双 Sha256 运算的结果前 4位 拼接到尾部    
    + BASE58编码    

+ LTC 地址格式       
  以下介绍 LTC 三种常见的地址格式。    
      
    + P2PKH 地址格式（imToken 支持）    
  P2PKH 格式（Pay to Public Key Hash）的地址以 L 开头，等同于 BTC 中以 1 开头的地址。P2PKH 地址格式示例：    
  LhyLNfBkoKshT7R8Pce6vkB9T2cP2o84hx    
      
    + P2SH-P2WPKH 地址格式（imToken 支持）    
  P2SH-P2WPKH 格式（Pay to Script Hash – Pay to Witness Public Key Hash）的地址以 M 开头，等同于 BTC 中以 3 开头的地址。P2SH-P2WPKH 地址格式：    
  MR5Hu9zXPX3o9QuYNJGft1VMpRP418QDfW    
  
    + Native SegWit 地址格式（imToken 暂不支持）    
  Native SegWit 格式的地址以 ltc 开头，等同于 BTC 中以 bc 开头的地址。Native SegWit 地址格式：    
  ltc1qum864wd9nwsc0u9ytkctz6wzrw6g7zdn08yddf    

+ BCH 地址格式           
    + Use CashAddr addresses for Bitcoin Cash (ie starting with 'q' instead of '1')    
    + Use BitPay-style addresses for Bitcoin Cash (ie starting with 'C' instead of '1')    
    + Use legacy addresses for Bitcoin Cash (ie starting with '1')              
              byte[] hashedPublicKey;    
              if (isCompressed()) {    
                  hashedPublicKey = RIPEMD160.hash160(pubComp);    
              } else {    
                  hashedPublicKey = RIPEMD160.hash160(pub);    
              }    
              return BitcoinCashAddressFormatter.toCashAddress(BitcoinCashAddressType.P2PKH,
                      hashedPublicKey, MoneyNetwork.MAIN);    
                      
                      

+ Filecoin 地址格式    
  Filecoin 有三种地址格式：普通地址（f1 开头）、矿工地址（f0 开头）和矿工地址（f3 开头。    
    + 普通地址格式（f1 开头）：f16tugakjlpyoomxy5uv2d6bdj7wcyr3ueofu7w7a    
    + 矿工地址格式（f0 开头）：f01782    
    + 矿工地址格式（f3 开头）：f3sg22lqqjewwczqcs2cjr3zp6htctbovwugzzut2nkvb366wzn5tp2zkfvu5xrfqhreowiryxump7l5e6jaaq       
 
## PS图层
+ 描述       
  图层动图合成，用于制作表情包或图片裁剪              
  表情说说 2018年项目       

+ 实现功能       
  涂鸦              
  图片自由裁剪       
  文字动效       
  动图合成       
  
+ 核心技术       
  页面重绘派发       
  Matrix矩阵控制       
  时间轴控制       
  
+ 代码       
```java
ARouter.getInstance().build(RoutePath.PS).navigation();
```
+ PSCanvas.java       
  描述: 制图主容器(画布)       
  1、循环线程RefreshRecordThread 控制子View(GIF,WEBP,文字动效)的刷新和最终的GIF合成       
  2、记录行为数组用于操纵用户操作记录，用于回撤和重做操作       
  合成流程说明：图片帧数<=1则直接生成一个PNG文件， 否则则是循环绘制每一帧（通过先绘制背景信息，然后绘制图层信息，没背景则对图片进行裁剪）       

  
+ PSLayer.java       
  描述: 图层，处理触屏事件用来放大缩小旋转操作     
  可以添加的操作视图：
    + CustomTextView 文字图层
    + CustomImageView 静图图层，包含涂鸦
    + CustomGifView gif动图图层 

+ GifMaker.java       
  描述: 绘制成Bitmap后添加到GifMaker之中，同时多个线程解析bitmap信息。最终生成Gif文件

## 插件动态加载
+ 描述       
  插件动态载入，加载未安装APK文件              
  360DroidPlugin项目重构，添加对高版本SDK兼容 2016       

+ 实现功能       
  免安装运行APK                    
  
+ 核心技术       
  反射，代理       
  版本兼容       
  AIDL       
  
+ 代码
```java
ARouter.getInstance().build(RoutePath.PLUGIN).navigation();
```
+ 核心     
```java  
           //hook PackageManager 拦截getPackageInfo类似请求，替换包名,使用动态代理
           mIPackageManagerHook = new ProxyHookPackageManager(mHostContext);
           installHookOnce(mIPackageManagerHook);
   
           //hook ActivityManager 拦截startActivity类似请求，替换intent，绕过AndroidManifest检测 使用动态代理
           installHookOnce(new ProxyHookActivityManager(mHostContext));
   
           //hook Handler的Callback， 在假的activity启动后替换为原本需要的activity并启动它 使用静态代理
           installHookOnce(new HookHandlerCallback(mHostContext));
   
           //hook Instrumentation 处理生命周期，替身的创建和销毁，并伪装系统服务 使用静态代理
           installHookOnce(new HookInstrumentation(mHostContext));
   
           connectToService(); 

```
  
+ AIDL       
  确保服务的唯一性，服务模仿系统的PackageManagerService，提供对插件简单的管理服务。

## 书籍阅读器

+ 描述       
  支持txt、umd、epub电子书格式文件本地阅读，支持在线阅读              
  乐阅 2014       

+ 实现功能       
  文件解析目录内容                    
  长按添加书签，书签绘制                    
  下拉添加笔记                    
  自定义排版                    
  
+ 核心技术       
  书籍解析       
  页面排版       
  动画控制       
  事件派发       
  
+ 代码
```java
ARouter.getInstance().build(RoutePath.BOOK).navigation();
```

+ ReadView.java       
  描述: 画布       
  操作：
  + 绑定解析器 PluginManager
  + 绑定页面管理器 PageManager
  + 绑定动画控制器 PageAnimController
    
+ PluginManager.java       
  描述: 书籍的解析器，将文件解析为StyleText树，用于PageManager进行页面排版       
  包含：
  + EpubPlugin epub型HTML解析
  + TextPlugin txt文件型解析
  + UmdPlugin  umd文件型解析 


+ PageManager.java       
  描述: 页面排版     
  ArrayList ChapterTask 每本书拥有的章节列表     
  LinkedList Page  每章节拥有的页列表     
  LinkedList Patch  每页面拥有的区域（Line行）     
  SparseArray RectF  每行拥有的矩阵     
  
  
+ PageAnimController.java       
  描述: 翻页动画控制器       
  包含：
  + PageTurningAnimController 仿真翻页动画
  + HorTranslationAnimController 横向平移动画 
  
  
+ 页面点击事件处理流程
  + 弹窗
  + 笔记
  + 竖向下拉
  + 横向翻页

## 算法

+ 图层算法PSOpView.java       
```java
    matrix.mapPoints(desPoints, srcPoints);

    float ox = center[0], oy = center[1];
    //获取到中心点位置 可得到位移X,Y
    center = new float[]{desPoints[0] + (desPoints[4] - desPoints[0]) / 2, desPoints[1] + (desPoints[5] - desPoints[1]) / 2};
    //获取旋转的角度0-360
    degree = MathUtil.getRotationBetweenLines(desPoints[6], desPoints[7], desPoints[0], desPoints[1]);

    double oriX = (srcPoints[2] - srcPoints[0]) * (srcPoints[2] - srcPoints[0]) + (srcPoints[3] - srcPoints[1]) * (srcPoints[3]
            - srcPoints[1]);
    oriX = Math.sqrt(oriX);
    double finX = (desPoints[2] - desPoints[0]) * (desPoints[2] - desPoints[0]) + (desPoints[3] - desPoints[1]) * (desPoints[3]
            - desPoints[1]);
    finX = Math.sqrt(finX);

    double oriY = (srcPoints[6] - srcPoints[0]) * (srcPoints[6] - srcPoints[0]) + (srcPoints[7] - srcPoints[1]) * (srcPoints[7]
            - srcPoints[1]);
    oriY = Math.sqrt(oriY);
    double finY = (desPoints[6] - desPoints[0]) * (desPoints[6] - desPoints[0]) + (desPoints[7] - desPoints[1]) * (desPoints[7]
            - desPoints[1]);
    finY = Math.sqrt(finY);
    //获取放大缩小的倍数
    scale = new float[]{(isRight ? 1 : -1) * (float) (finX / oriX), (float) (finY / oriY)};


    if (Math.abs(scale[0]) >= minScale[0] && scale[1] >= minScale[1]) {
        System.arraycopy(desPoints, 0, fixPoints, 0, 8);
    } else {//如果缩小超过最小scale，fixPoints修正为最小的坐标系
        Matrix matrixFix = new Matrix();
        float sx = (isRight ? 1 : -1) * Math.max(minScale[0], Math.abs(scale[0]));
        float sy = Math.max(minScale[1], scale[1]);
        matrixFix.postTranslate(center[0] - (rect.right - rect.left) / 2 + padding * 1, center[1] - (rect.bottom - rect.top) / 2
                + padding * 1);
        matrixFix.postScale(sx, sy, center[0], center[1]);
        matrixFix.postRotate(degree, center[0], center[1]);
        matrixFix.mapPoints(fixPoints, srcPoints);
    }
```

+ 翻页点线图       
![](https://github.com/Seasonallan/PSAndroid/blob/master/module_book/algorithm.jpg)



