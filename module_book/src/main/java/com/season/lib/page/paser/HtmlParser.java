package com.season.lib.page.paser;

import java.io.StringReader;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import com.season.lib.page.StyleText;
import com.season.lib.page.PageManager;
import com.season.lib.page.paser.html.DataProvider;
import com.season.lib.page.paser.html.ICssProvider;
import com.season.lib.page.paser.html.SurfingHtmlToSpannedConverter;
import com.season.lib.page.paser.html.tag.SizeInfo;

/**
 * 将HTML格式数据数据化StyleText
 *
 */
public class HtmlParser {

	public static final String TAG = HtmlParser.class.getSimpleName();
	private SurfingHtmlToSpannedConverter mConverter;

	public HtmlParser(ICssProvider cssProvider, DataProvider imageGetter, PageManager.TaskListener task
			, SizeInfo sizeInfo) {
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
		mConverter = new SurfingHtmlToSpannedConverter(cssProvider, imageGetter, parser, task,sizeInfo);
	}
	
	public StyleText getStyleText(){
		return mConverter.getStyleText();
	}

	
	public final void start(String source) throws RuntimeException{
		mConverter.convert(new InputSource(new StringReader(source)));
	}

}