package com.season.book.text.html;

import java.util.ArrayList;
import java.util.List;

import com.season.book.text.html.css.PropertyValue;
import com.season.book.text.html.HtmlParser.TagInfo;

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
	 * @param classTag
	 * @return 
	 */
	public List<PropertyValue> getClassInfo(List<TagInfo> tagInfos);
}
