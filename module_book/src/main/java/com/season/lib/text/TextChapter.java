package com.season.lib.text;

import android.text.TextUtils;

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
 * 章节控制
 * 
 * @author laijp
 * @date 2014-1-26
 * @email 451360508@qq.com
 */
public class TextChapter {

	public static final String DEFAULT_CHAPTER = "正文";
	private List<Catalog> mChapters;
	private int mCurrentChapter;
	private String mFilePath;
	private String mChapterPath;

	public TextChapter(String filePath, String chapterPath) {
		this.mChapterPath = chapterPath;
		this.mFilePath = filePath;
		this.mChapters = new ArrayList<Catalog>();
	}

	public void addChapterList(List<Catalog> chapter) {
		this.mChapters.addAll(chapter);
	}

	public void addChapter(String name, int position) {
		Catalog c = new Catalog(name, position);
		mChapters.add(c);
	}

	public int getNextChapterIndex() {
		if (mCurrentChapter < mChapters.size() - 1) {
			return mChapters.get(mCurrentChapter + 1).getIndex();
		}
		return 1;
	}

	public void setCurrentCHapterIndex(int index) {
		this.mCurrentChapter = index;
	}

	public int getCurrentChapterIndex() {
		return mCurrentChapter;
	}

	public int getCurrentChapterPosition() {
		if (mCurrentChapter >= mChapters.size()) {
			return 0;
		}
		return mChapters.get(mCurrentChapter).getIndex();
	}

	public List<Catalog> getChapters() {
		return mChapters;
	}

	/**
	 * 获取当前章节起始字节位置
	 *
	 * @param chapterIndex
	 * @return
	 */
	public int getChapterWordIndex(int chapterIndex) {
		if (mChapters != null && mChapters.size() > chapterIndex) {
			return mChapters.get(chapterIndex).getIndex();
		}
		return 0;
	}

	/**
	 * 通过阅读位置获取当前所在章节
	 *
	 * @param position
	 * @return
	 */
	public int getChapterIndexByPosition(int position) {
		if (mChapters != null && mChapters.size() > 0) {
			for (int i = 1; i < mChapters.size(); i++) {
				if (mChapters.get(i).getIndex() > position) {
					return i - 1;
				}
			}
			return mChapters.size() - 1;
		}
		return 0;
	}

	/**
	 * 获取当前章节名
	 * @return
	 */
	public String getCurrentChapterName() {
		if (mChapters == null || mChapters.size() == 0) {
			return DEFAULT_CHAPTER;
		}
		return mChapters.get(mCurrentChapter).getText();
	}



	private String buildChapterStr() throws Exception{
		if(mChapters == null){
			return "";
		}
		JSONArray json = new JSONArray();
		for (int i = 0; i < mChapters.size(); i++) {
			JSONObject obj = new JSONObject();
			Catalog chapter = mChapters.get(i);
			obj.put("name", chapter.getText());
			obj.put("position", chapter.getIndex());
			json.put(obj);
		}
		return json.toString();
	}

	private List<Catalog> buildChapterList(String str) throws Exception{
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
	
	private File getSaveFile(){
		String chapterFileName = mFilePath.hashCode()+"";
		File saveDir = new File(mChapterPath+"chapter/"+chapterFileName);
		return saveDir;
	}
	
	public void save(){
		final File saveDir = getSaveFile();
		if(saveDir.exists() && saveDir.isFile() && saveDir.length() > 4){
			return;
		}
		new Thread(){
			public void run(){ 
				saveDir.getParentFile().mkdirs();  
				try {
					saveFile(saveDir, mChapters);
				} catch (Exception e) {
				}
			}
		}.start();
	}

	private void saveFile(File saveDir, List<Catalog> chapters) throws Exception{
		saveDir.createNewFile(); // 创建新文件  
        BufferedWriter out = new BufferedWriter(new FileWriter(saveDir));  
        out.write(buildChapterStr()); // \r\n即为换行  
        out.flush(); // 把缓存区内容压入文件  
        out.close(); // 最后记得关闭文件   
	}
	
	public List<Catalog> readFile() throws Exception {
		File saveDir = getSaveFile();
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
