package com.season.example.transfer.serv.support;

import java.util.HashMap;
import java.util.Map;

/**
 * 进度缓存类
 * @author Join
 */
public class Progress {

    private static Map<String, Integer> progressMap = new HashMap<String, Integer>(); 
    private static Map<String, Long> sizeMap = new HashMap<String, Long>(); 

    public static void update(String fileName,  long fileSize, int progress) { 
        progressMap.put(fileName, progress);  
        sizeMap.put(fileName, fileSize);  
    }
    
    public static long getSize(String id){
    	Long s = sizeMap.get(id); 
    	return s == null ? 0 : s;
    }

    public static int getProgress(String id) {
        Integer p = progressMap.get(id);
        return p == null ? -1 : p;
    } 

    public static void clear() {
        progressMap.clear();
    }

}
