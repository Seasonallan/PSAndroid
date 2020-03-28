/*    */ package com.season.example.transfer.fileupload.util;
/*    */ 
/*    */ import java.io.InputStream;
/*    */ 
/*    */ public class ClosedInputStream extends InputStream
/*    */ {
/* 37 */   public static final ClosedInputStream CLOSED_INPUT_STREAM = new ClosedInputStream();
/*    */ 
/*    */   public int read()
/*    */   {
/* 46 */     return -1;
/*    */   }
/*    */ }

/* Location:           C:\Users\Administrator\Desktop\commons-io-2.4.jar
 * Qualified Name:     org.apache.commons.io.input.ClosedInputStream
 * JD-Core Version:    0.6.0
 */