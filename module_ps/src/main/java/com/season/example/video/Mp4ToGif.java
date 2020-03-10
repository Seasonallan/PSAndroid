package com.season.example.video;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.season.lib.BaseContext;
import com.season.lib.gif.base.LZWEncoder;
import com.season.lib.gif.extend.LZWEncoderOrderHolder;
import com.season.lib.gif.extend.ThreadGifEncoder;
import com.season.lib.util.LogUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Mp4ToGif {

    private static final long DEFAULT_TIMEOUT_US = 10000;

    private  static final int COLOR_FormatI420 = 1;
    public  static final int COLOR_FormatNV21 = 2;

    BufferedOutputStream bosToFile;
    public String filePath;
    public Mp4ToGif(String filePath){
        this.filePath = filePath;
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            bosToFile = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<String> videoDecode(Context context, Uri uri, int startTime, int endTime) throws IOException {
        final int decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
        MediaExtractor extractor = null;
        MediaCodec decoder = null;
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(context, uri, null);
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + uri);
            }
            extractor.selectTrack(trackIndex);
            MediaFormat mediaFormat = extractor.getTrackFormat(trackIndex);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            if (isColorFormatSupported(decodeColorFormat, decoder.getCodecInfo().getCapabilitiesForType(mime))) {
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat);
            }

            return decodeFramesToImage(decoder, extractor, mediaFormat, startTime, endTime);

        } finally {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        }
    }


    private boolean isColorFormatSupported(int colorFormat, MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            if (c == colorFormat) {
                return true;
            }
        }
        return false;
    }

    private long current = 0;
    private final long MI_SECOND = 1000;
    private final long SECOND = MI_SECOND * MI_SECOND;
    public boolean isLowerDivice = false;
    public int hightQ = 20;
    public int lowQ = 50;//质量1～255，1最高清
    public long dts = 0;
    private List<String> decodeFramesToImage(MediaCodec decoder, MediaExtractor extractor, MediaFormat mediaFormat, int startTime, int endTime) throws IOException {
        List<String> list = new ArrayList<>();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        decoder.configure(mediaFormat, null, null, 0);
        decoder.start();
        dts = 0;
        final int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        final int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        int outputFrameCount = 0;
        while (!sawOutputEOS) {
            if (!sawInputEOS) {
                int inputBufferId = decoder.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
                if (inputBufferId >= 0) {
                    ByteBuffer inputBuffer = decoder.getInputBuffer(inputBufferId);
                    int sampleSize = extractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        sawInputEOS = true;
                    } else {
                        dts = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufferId, 0, sampleSize, dts, 0);
                        extractor.advance();
                    }
                }
            }
            int outputBufferId = decoder.dequeueOutputBuffer(info, DEFAULT_TIMEOUT_US);
            if (outputBufferId >= 0) {
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    LogUtil.e( "------------end>>" );
                    sawOutputEOS = true;
                }
                boolean doRender = (info.size != 0);
                if (doRender) {
                    LogUtil.e(dts + ">>" + current +"<<"+ endTime * MI_SECOND);
                    Image image = decoder.getOutputImage(outputBufferId);
                    if (current == 0){
                        current = dts;
                    }if(dts < current){

                    }else{
                        sawOutputEOS = dts + (dts - current) > endTime * MI_SECOND;
                        LogUtil.e(sawOutputEOS + ">>"+ (int) ((dts - current)/MI_SECOND) + "   " + outputFrameCount);
                        ByteArrayOutputStream currentStream = new ByteArrayOutputStream();
                        ThreadGifEncoder encoder = new ThreadGifEncoder();
                        encoder.setQuality(isLowerDivice ? lowQ : hightQ);
                        encoder.setDelay((int) ((dts - current)/MI_SECOND));
                        encoder.start(currentStream, outputFrameCount);
                        encoder.setFirstFrame(outputFrameCount == 0);
                        encoder.setRepeat(0);
                        LZWEncoderOrderHolder holder = encoder.addFrame(compressToJpeg(image), outputFrameCount);
                        encoder.finishThread(sawOutputEOS, holder.getLZWEncoder());
                        holder.setByteArrayOutputStream(currentStream);
                        bosToFile.write(holder.getByteArrayOutputStream().toByteArray());
                        holder.release();
                        current = dts;
                        outputFrameCount++;
                    }

                    image.close();
                    decoder.releaseOutputBuffer(outputBufferId, true);
                }
            }
        }
        bosToFile.flush();
        bosToFile.close();
        return list;
    }



    private int selectTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    public static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }


    private Bitmap compressToJpeg(Image image) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        Rect rect = image.getCropRect();
         YuvImage yuvImage = new YuvImage(getDataFromImage(image, COLOR_FormatNV21), ImageFormat.NV21, rect.width(), rect.height(), null);
        yuvImage.compressToJpeg(rect, 100, outStream);
        byte[] jdata = outStream.toByteArray();
        return BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
    }


}