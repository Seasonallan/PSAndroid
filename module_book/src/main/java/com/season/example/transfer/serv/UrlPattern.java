package com.season.example.transfer.serv;

public class UrlPattern {

    /** 浏览，默认 */
    public static final String BROWSE = "*"; 
    /** 下载 */
    public static final String DOWNLOAD = "/files_download";
    /** 删除 */
    public static final String DELETE = "/deletefile";
    /** 上传 */
    public static final String UPLOAD = "/files";
    /** 进度 */
    public static final String PROGRESS = "/doprogress"; 
    /** 获取已上传文件 */
    public static final String GET_FILES = "/files_get";
}
