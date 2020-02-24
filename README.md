# PS图层&阅读器

## PS图层

+ 入口
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
    + CustomGifMovie gif动图图层
    + CustomGifFrame gif动图图层，只有在GifMovieView解析失败的情况下会用    

+ GifMaker.java       
  描述: 绘制成Bitmap后添加到GifMaker之中，同时多个线程解析bitmap信息。最终生成Gif文件

## 书籍阅读器

+ 入口
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
  ArrayList<ChapterTask> 每本书拥有的章节列表     
  LinkedList<Page> 每章节拥有的页列表     
  LinkedList<Patch> 每页面拥有的区域（Line行）     
  SparseArray<RectF> 每行拥有的矩阵     
  
  
+ PageAnimController.java       
  描述: 翻页动画控制器       
  包含：
  + PageTurningAnimController 仿真翻页动画
  + HorTranslationAnimController 横向平移动画 
  
  
+ 页面点击事件处理流程
```java
@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
  //首次绘制是否结束，等待书籍解析布局完毕
  if(mReadView.onActivityDispatchTouchEvent(ev)){
    return false;
  }
  //目录弹窗是否显示
  if(mCatalogLay.isShown()){
    return super.dispatchTouchEvent(ev);
  }
  //长按选中操作中
  if(mReadView.handlerSelectTouchEvent(ev, this)){
    return false;
  }
  //单击长按事件派发处理，特别注意未处理事件时派发ACTION_DOWN事件，防止丢失
  if(mClickDetector.onTouchEvent(ev, false)){
    return false;
  }
  return super.dispatchTouchEvent(ev);
}
```


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



