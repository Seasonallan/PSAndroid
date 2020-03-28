/*      */ package com.season.example.transfer.fileupload.util;
/*      */ 
/*      */ import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.channels.Selector;
/*      */ 
/*      */ public class IOUtils
/*      */ { 
/*      */   public static final char DIR_SEPARATOR_UNIX = '/';
/*      */   public static final char DIR_SEPARATOR_WINDOWS = '\\';
/*  101 */   public static final char DIR_SEPARATOR = File.separatorChar;
/*      */   public static final String LINE_SEPARATOR_UNIX = "\n";
/*      */   public static final String LINE_SEPARATOR_WINDOWS = "\r\n";  
/*      */   private static char[] SKIP_CHAR_BUFFER;
/*      */   private static byte[] SKIP_BYTE_BUFFER;
/*      */ 
/*      */   public static void close(URLConnection conn)
/*      */   {
/*  164 */     if ((conn instanceof HttpURLConnection))
/*  165 */       ((HttpURLConnection)conn).disconnect();
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(Reader input)
/*      */   {
/*  193 */     closeQuietly(input);
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(Writer output)
/*      */   {
/*  219 */     closeQuietly(output);
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(InputStream input)
/*      */   {
/*  246 */     closeQuietly(input);
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(OutputStream output)
/*      */   {
/*  274 */     closeQuietly(output);
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(Closeable closeable)
/*      */   {
/*      */     try
/*      */     {
/*  302 */       if (closeable != null)
/*  303 */         closeable.close();
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(Socket sock)
/*      */   {
/*  334 */     if (sock != null)
/*      */       try {
/*  336 */         sock.close();
/*      */       }
/*      */       catch (IOException ioe)
/*      */       {
/*      */       }
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(Selector selector)
/*      */   {
/*  367 */     if (selector != null)
/*      */       try {
/*  369 */         selector.close();
/*      */       }
/*      */       catch (IOException ioe)
/*      */       {
/*      */       }
/*      */   }
/*      */ 
/*      */   public static void closeQuietly(ServerSocket sock)
/*      */   {
/*  400 */     if (sock != null)
/*      */       try {
/*  402 */         sock.close();
/*      */       }
/*      */       catch (IOException ioe)
/*      */       {
/*      */       }
/*      */   }
/*      */  
/*      */  
/*      */ 
/*      */   public static int copy(InputStream input, OutputStream output)
/*      */     throws IOException
/*      */   {
/* 1744 */     long count = copyLarge(input, output);
/* 1745 */     if (count > 2147483647L) {
/* 1746 */       return -1;
/*      */     }
/* 1748 */     return (int)count;
/*      */   }
/*      */ 
/*      */   public static long copyLarge(InputStream input, OutputStream output)
/*      */     throws IOException
/*      */   {
/* 1769 */     return copyLarge(input, output, new byte[4096]);
/*      */   }
/*      */ 
/*      */   public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
/*      */     throws IOException
/*      */   {
/* 1790 */     long count = 0L;
/* 1791 */     int n = 0;
/* 1792 */     while (-1 != (n = input.read(buffer))) {
/* 1793 */       output.write(buffer, 0, n);
/* 1794 */       count += n;
/*      */     }
/* 1796 */     return count;
/*      */   }
/*      */ 
/*      */   public static long copyLarge(InputStream input, OutputStream output, long inputOffset, long length)
/*      */     throws IOException
/*      */   {
/* 1820 */     return copyLarge(input, output, inputOffset, length, new byte[4096]);
/*      */   }
/*      */ 
/*      */   public static long copyLarge(InputStream input, OutputStream output, long inputOffset, long length, byte[] buffer)
/*      */     throws IOException
/*      */   {
/* 1845 */     if (inputOffset > 0L) {
/* 1846 */       skipFully(input, inputOffset);
/*      */     }
/* 1848 */     if (length == 0L) {
/* 1849 */       return 0L;
/*      */     }
/* 1851 */     int bufferLength = buffer.length;
/* 1852 */     int bytesToRead = bufferLength;
/* 1853 */     if ((length > 0L) && (length < bufferLength)) {
/* 1854 */       bytesToRead = (int)length;
/*      */     }
/*      */ 
/* 1857 */     long totalRead = 0L;
/*      */     int read;
/* 1858 */     while ((bytesToRead > 0) && (-1 != (read = input.read(buffer, 0, bytesToRead)))) {
/* 1859 */       output.write(buffer, 0, read);
/* 1860 */       totalRead += read;
/* 1861 */       if (length <= 0L)
/*      */         continue;
/* 1863 */       bytesToRead = (int)Math.min(length - totalRead, bufferLength);
/*      */     }
/*      */ 
/* 1866 */     return totalRead;
/*      */   }
/*      */  
/*      */ 
/*      */   public static long copyLarge(Reader input, Writer output, long inputOffset, long length)
/*      */     throws IOException
/*      */   {
/* 2029 */     return copyLarge(input, output, inputOffset, length, new char[4096]);
/*      */   }
/*      */ 
/*      */   public static long copyLarge(Reader input, Writer output, long inputOffset, long length, char[] buffer)
/*      */     throws IOException
/*      */   {
/* 2053 */     if (inputOffset > 0L) {
/* 2054 */       skipFully(input, inputOffset);
/*      */     }
/* 2056 */     if (length == 0L) {
/* 2057 */       return 0L;
/*      */     }
/* 2059 */     int bytesToRead = buffer.length;
/* 2060 */     if ((length > 0L) && (length < buffer.length)) {
/* 2061 */       bytesToRead = (int)length;
/*      */     }
/*      */ 
/* 2064 */     long totalRead = 0L;
/*      */     int read;
/* 2065 */     while ((bytesToRead > 0) && (-1 != (read = input.read(buffer, 0, bytesToRead)))) {
/* 2066 */       output.write(buffer, 0, read);
/* 2067 */       totalRead += read;
/* 2068 */       if (length <= 0L)
/*      */         continue;
/* 2070 */       bytesToRead = (int)Math.min(length - totalRead, buffer.length);
/*      */     }
/*      */ 
/* 2073 */     return totalRead;
/*      */   }
/*      */  
/*      */   public static boolean contentEquals(InputStream input1, InputStream input2)
/*      */     throws IOException
/*      */   {
/* 2179 */     if (!(input1 instanceof BufferedInputStream)) {
/* 2180 */       input1 = new BufferedInputStream(input1);
/*      */     }
/* 2182 */     if (!(input2 instanceof BufferedInputStream)) {
/* 2183 */       input2 = new BufferedInputStream(input2);
/*      */     }
/*      */ 
/* 2186 */     int ch = input1.read();
/* 2187 */     while (-1 != ch) {
/* 2188 */       int ch2 = input2.read();
/* 2189 */       if (ch != ch2) {
/* 2190 */         return false;
/*      */       }
/* 2192 */       ch = input1.read();
/*      */     }
/*      */ 
/* 2195 */     int ch2 = input2.read();
/* 2196 */     return ch2 == -1;
/*      */   }
/*      */  
/*      */ 
/*      */   public static long skip(InputStream input, long toSkip)
/*      */     throws IOException
/*      */   {
/* 2278 */     if (toSkip < 0L) {
/* 2279 */       throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
/*      */     }
/*      */ 
/* 2286 */     if (SKIP_BYTE_BUFFER == null) {
/* 2287 */       SKIP_BYTE_BUFFER = new byte[2048];
/*      */     }
/* 2289 */     long remain = toSkip;
/* 2290 */     while (remain > 0L) {
/* 2291 */       long n = input.read(SKIP_BYTE_BUFFER, 0, (int)Math.min(remain, 2048L));
/* 2292 */       if (n < 0L) {
/*      */         break;
/*      */       }
/* 2295 */       remain -= n;
/*      */     }
/* 2297 */     return toSkip - remain;
/*      */   }
/*      */ 
/*      */   public static long skip(Reader input, long toSkip)
/*      */     throws IOException
/*      */   {
/* 2317 */     if (toSkip < 0L) {
/* 2318 */       throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
/*      */     }
/*      */ 
/* 2325 */     if (SKIP_CHAR_BUFFER == null) {
/* 2326 */       SKIP_CHAR_BUFFER = new char[2048];
/*      */     }
/* 2328 */     long remain = toSkip;
/* 2329 */     while (remain > 0L) {
/* 2330 */       long n = input.read(SKIP_CHAR_BUFFER, 0, (int)Math.min(remain, 2048L));
/* 2331 */       if (n < 0L) {
/*      */         break;
/*      */       }
/* 2334 */       remain -= n;
/*      */     }
/* 2336 */     return toSkip - remain;
/*      */   }
/*      */ 
/*      */   public static void skipFully(InputStream input, long toSkip)
/*      */     throws IOException
/*      */   {
/* 2355 */     if (toSkip < 0L) {
/* 2356 */       throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
/*      */     }
/* 2358 */     long skipped = skip(input, toSkip);
/* 2359 */     if (skipped != toSkip)
/* 2360 */       throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
/*      */   }
/*      */ 
/*      */   public static void skipFully(Reader input, long toSkip)
/*      */     throws IOException
/*      */   {
/* 2380 */     long skipped = skip(input, toSkip);
/* 2381 */     if (skipped != toSkip)
/* 2382 */       throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
/*      */   }
/*      */ 
/*      */   public static int read(Reader input, char[] buffer, int offset, int length)
/*      */     throws IOException
/*      */   {
/* 2402 */     if (length < 0) {
/* 2403 */       throw new IllegalArgumentException("Length must not be negative: " + length);
/*      */     }
/* 2405 */     int remaining = length;
/* 2406 */     while (remaining > 0) {
/* 2407 */       int location = length - remaining;
/* 2408 */       int count = input.read(buffer, offset + location, remaining);
/* 2409 */       if (-1 == count) {
/*      */         break;
/*      */       }
/* 2412 */       remaining -= count;
/*      */     }
/* 2414 */     return length - remaining;
/*      */   }
/*      */ 
/*      */   public static int read(Reader input, char[] buffer)
/*      */     throws IOException
/*      */   {
/* 2430 */     return read(input, buffer, 0, buffer.length);
/*      */   }
/*      */ 
/*      */   public static int read(InputStream input, byte[] buffer, int offset, int length)
/*      */     throws IOException
/*      */   {
/* 2448 */     if (length < 0) {
/* 2449 */       throw new IllegalArgumentException("Length must not be negative: " + length);
/*      */     }
/* 2451 */     int remaining = length;
/* 2452 */     while (remaining > 0) {
/* 2453 */       int location = length - remaining;
/* 2454 */       int count = input.read(buffer, offset + location, remaining);
/* 2455 */       if (-1 == count) {
/*      */         break;
/*      */       }
/* 2458 */       remaining -= count;
/*      */     }
/* 2460 */     return length - remaining;
/*      */   }
/*      */ 
/*      */   public static int read(InputStream input, byte[] buffer)
/*      */     throws IOException
/*      */   {
/* 2476 */     return read(input, buffer, 0, buffer.length);
/*      */   }
/*      */ 
/*      */   public static void readFully(Reader input, char[] buffer, int offset, int length)
/*      */     throws IOException
/*      */   {
/* 2496 */     int actual = read(input, buffer, offset, length);
/* 2497 */     if (actual != length)
/* 2498 */       throw new EOFException("Length to read: " + length + " actual: " + actual);
/*      */   }
/*      */ 
/*      */   public static void readFully(Reader input, char[] buffer)
/*      */     throws IOException
/*      */   {
/* 2517 */     readFully(input, buffer, 0, buffer.length);
/*      */   }
/*      */ 
/*      */   public static void readFully(InputStream input, byte[] buffer, int offset, int length)
/*      */     throws IOException
/*      */   {
/* 2537 */     int actual = read(input, buffer, offset, length);
/* 2538 */     if (actual != length)
/* 2539 */       throw new EOFException("Length to read: " + length + " actual: " + actual);
/*      */   }
/*      */ 
/*      */   public static void readFully(InputStream input, byte[] buffer)
/*      */     throws IOException
/*      */   {
/* 2558 */     readFully(input, buffer, 0, buffer.length);
/*      */   }
/*      */  
/*      */ }

/* Location:           C:\Users\Administrator\Desktop\commons-io-2.4.jar
 * Qualified Name:     org.apache.commons.io.IOUtils
 * JD-Core Version:    0.6.0
 */