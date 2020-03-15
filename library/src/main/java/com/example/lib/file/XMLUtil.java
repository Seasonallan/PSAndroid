package com.example.lib.file;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

/** XML辅助工具类
 */
public final class XMLUtil {
	
	/**
	 * SAX解析XML
	 * 
	 * @param handler
	 * @param content
	 */
	public static boolean parserXml(DefaultHandler handler, byte[] content) {
		if(handler == null || content == null){
			return false;
		}
		try {
			InputStream is = new ByteArrayInputStream(content);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(is, handler);
			is.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
