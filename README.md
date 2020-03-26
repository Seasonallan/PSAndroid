# PS图层&插件化&阅读器
+ [测试APK下载](https://github.com/Seasonallan/PSAndroid/blob/master/apk/season.apk)
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



