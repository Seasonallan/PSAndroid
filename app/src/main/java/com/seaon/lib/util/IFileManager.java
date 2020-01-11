package com.seaon.lib.util;

import java.io.File;

/**
 * Created by lizhongxin on 13/10/2018.
 */

public interface IFileManager {
    /**
     * 得到指定分辨率的水印文件，水印是自己画的，异步处理
     * @param resolution
     */
    File getWaterMarkFile(int resolution);
}
