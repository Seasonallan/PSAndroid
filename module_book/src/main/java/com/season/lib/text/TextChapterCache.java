package com.season.lib.text;

import android.text.TextUtils;

import com.season.lib.BaseContext;
import com.season.lib.bean.Catalog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 章节缓存
 */
public class TextChapterCache {


	private static String buildChapterStr( ArrayList<Catalog> catalog) throws Exception{
		if(catalog == null){
			return "";
		}
		JSONArray json = new JSONArray();
		for (int i = 0; i < catalog.size(); i++) {
			JSONObject obj = new JSONObject();
			Catalog chapter = catalog.get(i);
			obj.put("name", chapter.getText());
			obj.put("position", chapter.getIndex());
			json.put(obj);
		}
		return json.toString();
	}

	private static List<Catalog> buildChapterList(String str) throws Exception{
		if(TextUtils.isEmpty(str)){
			return null;
		}
		List<Catalog> res = new ArrayList<Catalog>();
		JSONArray json = new JSONArray(str);
		for (int i = 0; i < json.length(); i++) {
			JSONObject obj = json.getJSONObject(i);
			Catalog chapter = new Catalog(obj.getString("name"), obj.getInt("position"));
			res.add(chapter);
		}
		return res;
	}
	
	private static File getSaveFile(String filePath){
		String chapterFileName = filePath.hashCode()+"";
		File saveDir = new File(BaseContext.getInstance().getCacheDir()+"/chapters/"+chapterFileName);
		return saveDir;
	}
	
	public static void save(String filePath, final ArrayList<Catalog> catalog){
		final File saveDir = getSaveFile(filePath);
		if(saveDir.exists() && saveDir.isFile() && saveDir.length() > 4){
			return;
		}
		new Thread(){
			public void run(){ 
				saveDir.getParentFile().mkdirs();  
				try {
					saveFile(saveDir, catalog);
				} catch (Exception e) {
				}
			}
		}.start();
	}

	private static void saveFile(File saveDir, ArrayList<Catalog> chapters) throws Exception{
		saveDir.createNewFile(); // 创建新文件  
        BufferedWriter out = new BufferedWriter(new FileWriter(saveDir));  
        out.write(buildChapterStr(chapters)); // \r\n即为换行
        out.flush(); // 把缓存区内容压入文件  
        out.close(); // 最后记得关闭文件   
	}
	
	public static List<Catalog> readFile(String filePath) throws Exception {
		File saveDir = getSaveFile(filePath);
		if (saveDir.exists() && saveDir.isFile() && saveDir.length() > 4) {
			StringBuffer res = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader(saveDir));
			String tempString = null; 
			while ((tempString = reader.readLine()) != null) {
				res.append(tempString);
			}
			reader.close();
			return buildChapterList(res.toString());
		}
		return null;
	}
	
}
