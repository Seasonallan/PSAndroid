package com.example.lib.page.paser.html;

import java.util.ArrayList;
import java.util.List;

import com.example.lib.page.paser.html.css.PropertyValue;
import com.example.lib.page.paser.html.tag.TagInfo;

/**
 * Css提供者，负责获取解析CSS
 * @author lyw
 *
 */
public interface ICssProvider {
	/**
	 * 执行解析Css
	 */
	public void parse(ArrayList<String> paths);
	/**
	 * 获取Css样式内容
	 * @param tagInfos
	 * @return 
	 */
	public List<PropertyValue> getClassInfo(List<TagInfo> tagInfos);
}
