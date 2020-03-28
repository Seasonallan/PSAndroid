package com.season.example.transfer.fileupload.httpserv;

import java.io.IOException;

import com.season.example.transfer.fileupload.FileItemFactory;
import com.season.example.transfer.fileupload.FileItemIterator;
import com.season.example.transfer.fileupload.FileUpload;
import com.season.example.transfer.fileupload.FileUploadBase;
import com.season.example.transfer.fileupload.FileUploadException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

public class HttpServFileUpload extends FileUpload {

    /** Constant for HTTP POST method. */
    private static final String POST_METHOD = "POST";

    public static final boolean isMultipartContent(HttpRequest request) {
        String method = request.getRequestLine().getMethod();
        if (!POST_METHOD.equalsIgnoreCase(method)) {
            return false;
        }
        if (!(request instanceof HttpEntityEnclosingRequest)) {
            return false;
        }
        return FileUploadBase.isMultipartContent(new HttpServRequestContext(
                (HttpEntityEnclosingRequest) request));
    }

    public HttpServFileUpload() {
        super();
    }

    public HttpServFileUpload(FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }
 

    public FileItemIterator getItemIterator(HttpRequest request) throws FileUploadException,
            IOException {
        return super.getItemIterator(new HttpServRequestContext(request));
    }

}
