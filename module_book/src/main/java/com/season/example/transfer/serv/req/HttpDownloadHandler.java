package com.season.example.transfer.serv.req;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import com.season.example.transfer.serv.support.HttpGetParser;
import com.season.example.transfer.util.Constants;

/**
 * @brief 下载请求处理
 * @author join
 */
public class HttpDownloadHandler implements HttpRequestHandler {
  

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {  
        HttpGetParser parser = new HttpGetParser();
        Map<String, String> params = parser.parse(request); 
        String fname = params.get("fname"); 
        if (fname == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        fname = URLDecoder.decode(fname, Constants.ENCODING); 
        final File file = new File(Constants.UPLOAD_DIR, fname);
        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException { 
            	write(file, outstream); 
            }
        });
        response.setStatusCode(HttpStatus.SC_OK);
        response.addHeader("Content-Description", "File Transfer");
        response.setHeader("Content-Type", "application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=" + encodeFilename(file));
        response.setHeader("Content-Transfer-Encoding", "binary");
        // 在某平板自带浏览器上下载失败，比较下能成功下载的响应头，这里少了Content-Length。但设了，反而下不了了。
        response.setEntity(entity);
 
    }

    private String encodeFilename(File file) throws IOException { 
        String filename = URLEncoder.encode(getFilename(file), Constants.ENCODING);
        return filename.replace("+", "%20");
    }

    private String getFilename(File file) {
        return file.isFile() ? file.getName() : file.getName() + ".zip";
    }

    /**
     * @brief 写入文件
     * @param inputFile 输入文件
     * @param outstream 输出流
     * @throws IOException
     */
    private void write(File inputFile, OutputStream outstream) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        try {
            int count;
            byte[] buffer = new byte[Constants.BUFFER_LENGTH];
            while ((count = fis.read(buffer)) != -1) {
                outstream.write(buffer, 0, count);
            }
            outstream.flush();
        } catch (IOException e) { 
            e.printStackTrace();
            throw e;
        } finally {
            fis.close();
            outstream.close();
        }
    } 
}
