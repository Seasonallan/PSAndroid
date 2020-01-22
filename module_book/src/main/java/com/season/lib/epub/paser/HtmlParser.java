package com.season.lib.epub.paser;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.season.lib.epub.bean.layout.StyleText;
import com.season.lib.epub.page.PageManager;
import com.season.lib.epub.paser.html.DataProvider;
import com.season.lib.epub.paser.html.ICssProvider;
import com.season.lib.epub.paser.html.SurfingHtmlToSpannedConverter;
import com.season.lib.epub.paser.html.tag.SizeInfo;
import com.season.lib.epub.paser.html.tag.TagHandler;

/**
 * 将HTML格式数据数据化StyleText
 *
 */
public class HtmlParser {

	public static final String TAG = HtmlParser.class.getSimpleName();
	private SurfingHtmlToSpannedConverter mConverter;
	public static HtmlParser create(ICssProvider cssProvider, DataProvider imageGetter
			, PageManager.TaskListener task, TagHandler tagHandler, SizeInfo sizeInfo){
		return new HtmlParser(cssProvider, imageGetter, task, tagHandler,sizeInfo);
	}
	
	protected HtmlParser(ICssProvider cssProvider, DataProvider imageGetter,PageManager.TaskListener task
			,TagHandler tagHandler,SizeInfo sizeInfo) {
		XMLReader parser = null;
		try {
			//tagsoup 的解析方式
//			parser = new Parser();
//			parser.setProperty(Parser.schemaProperty,new HTMLSchema());
			//原始的SAX解析方式 
			SAXParserFactory factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser().getXMLReader();
		} catch (org.xml.sax.SAXNotRecognizedException e) {
			// Should not happen.
			throw new RuntimeException(e);
		} catch (org.xml.sax.SAXNotSupportedException e) {
			// Should not happen.
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		mConverter = createHandler(cssProvider,imageGetter, parser,task,tagHandler,sizeInfo);
	}
	
	public StyleText getStyleText(){
		return mConverter.getStyleText();
	}

	
	public final void start(String source) throws RuntimeException{
		mConverter.convert(new InputSource(new StringReader(source)));
	}

	
	protected SurfingHtmlToSpannedConverter createHandler(ICssProvider cssProvider, DataProvider imageGetter
			, XMLReader parser, PageManager.TaskListener task, TagHandler tagHandler, SizeInfo sizeInfo){
		return new SurfingHtmlToSpannedConverter(cssProvider, imageGetter, parser, task, tagHandler,sizeInfo);
	}

}