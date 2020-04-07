package com.season.book.plugin.epub;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.text.TextUtils;

import com.season.lib.file.IOUtil;
import com.season.lib.file.XMLUtil;
import com.season.book.bean.Catalog;
import com.season.book.plugin.PluginManager;
import com.season.lib.util.LogUtil;

/** EPUB格式书籍的解析
 * @author mingkg21
 * @email mingkg21@gmail.com
 * @date 2013-2-19
 */
public class EpubPlugin extends PluginManager {
	
	private static final String DEFAULT_OPF_FILE_LOCATION = "OEBPS/content.opf";
	private static final String CONTAINER_FILE_LOCATION = "META-INF/container.xml";
	private String opfFilePath;
	private HashMap<String, Resource> manifestIdResources;
	private HashMap<String, Resource> manifestHrefResources;
	private HashMap<String,Catalog> catalogHrefMap;
	private HashMap<String, Resource> allResources = new HashMap<String, Resource>();

	/** 书籍章节ID列表 */
	private ArrayList<String> chapterIds = new ArrayList<String>();

	public EpubPlugin(String filePath) {
		super(filePath);
	}

	public void init() throws Exception {
		boolean hadContainerfile = false;
		ZipFile zipFile = new ZipFile(filePath);
		ZipEntry zipEntry = null;
		Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
		if(enumeration != null){
			for (;enumeration.hasMoreElements();) {
				zipEntry = enumeration.nextElement();
				String name = zipEntry.getName();
				//LogUtil.i(">>zip entry name: ", zipEntry.getName());
				if(zipEntry.isDirectory()){
					continue;
				}
				allResources.put(name, new Resource(filePath, name));
				if(CONTAINER_FILE_LOCATION.equals(name)){//解析container.xml文件获取OPF文件路径
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					byte[] data = IOUtil.toByteArray(inputStream);
					inputStream.close();
					ContainerDecoder handler = new ContainerDecoder();
					if(XMLUtil.parserXml(handler, data)){
						opfFilePath = handler.getOpfFilePath();
						hadContainerfile = true;
						LogUtil.i("opf file filePath: ", opfFilePath);
					}
				}
				if(opfFilePath != null && opfFilePath.equals(name)){//查找OFP文件，并解析
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					initBookInfo(inputStream);
					inputStream.close();
				}
			}
		}
		zipFile.close();
		if(bookInfo == null){//如果之前没解析到，重新解析获取书籍信息
			if(opfFilePath == null){
				opfFilePath = DEFAULT_OPF_FILE_LOCATION;
			}
			zipFile = new ZipFile(filePath);
			enumeration = zipFile.entries();
			if(enumeration != null){
				for (;enumeration.hasMoreElements();) {
					zipEntry = enumeration.nextElement();
					String name = zipEntry.getName();
					//LogUtil.i("zip entry name: ", zipEntry.getName());
					if(zipEntry.isDirectory()){
						continue;
					}
					if(opfFilePath != null && opfFilePath.equals(name)){//查找OFP文件，并解析
						initBookInfo(zipFile.getInputStream(zipEntry));
					}
				}
			}
			zipFile.close();
		}
		if(!hadContainerfile){//没找到META-INF/container.xml文件，则认为不是正确的EPUB格式书籍
			throw new Exception(filePath + " isn't a epub epub_book file!");
		}
	}


	/** 获取书籍目录；不为NULL，如果没有目录，SIZE为0
	 * @return the catalog
	 */
	public ArrayList<Catalog> getCatalog() {
		return catalog;
	}

	/**
	 * @return the chapterIds
	 */
	public ArrayList<String> getChapterIds() {
		return chapterIds;
	}

	@Override
	public String getFixHtml(String content) {
		StringBuffer temp = new StringBuffer();
		temp.append("<html><body>");
		temp.append(content);
		temp.append("</body></html>");
		return temp.toString();
	}

	@Override
	public int getChapterPosition(String chapterID) {
		return getChapterIds().indexOf(chapterID);
	}

	@Override
	public String getChapterId(int chapterIndex) {
		return getChapterIds().get(chapterIndex);
	}

	protected void setCatalog(List<Catalog> catalogs){
		if(catalogs == null){
			return;
		}
		this.catalog.clear();
		this.catalog.addAll(catalogs);
	}

	protected void setChapterIds(List<String> chapterIds){
		if(chapterIds == null){
			return;
		}
		this.chapterIds.clear();
		this.chapterIds.addAll(chapterIds);
	}

	/** 初始化书籍信息
	 * @param zis
	 * @throws IOException
	 */
	private void initBookInfo(InputStream zis) throws IOException {
		//LogUtil.i("initBookInfo: ");
		byte[] data = IOUtil.toByteArray(zis);
		EpubFileDecoder handler = new EpubFileDecoder(filePath);
		if(XMLUtil.parserXml(handler, data)){
			//解析书籍信息
			bookInfo = handler.getBookInfo();
			//获取目录位置
			setChapterIds(handler.getChapterIds());
			//缓存资源文件位置
			manifestIdResources = handler.getIdResources();
			manifestHrefResources = handler.getHrefResources();
			contentCover = handler.contentCover;
			String navFilePath = handler.getNavFilePath();
			String ncxId = handler.getNcxId();
			//解析目录
			if(manifestIdResources != null){
				if(!TextUtils.isEmpty(navFilePath)){
					Resource resource = manifestHrefResources.get(navFilePath);
					if(resource != null){
						CatalogNAVDecoder catalogNavDecoder = new CatalogNAVDecoder(navFilePath);
						byte[] navData = resource.getData();
						//navData = EncryptUtils.decryptByAES(navData);
						if(XMLUtil.parserXml(catalogNavDecoder, navData)){
							ArrayList<Catalog> catalogs = catalogNavDecoder.getCatalogs();
							catalogHrefMap = catalogNavDecoder.getCatalogHrefMap();
							setCatalog(catalogs);
						}
					}
				}
				if(getCatalog().size() == 0 && !TextUtils.isEmpty(ncxId)){
					Resource resource = manifestIdResources.get(ncxId);
					if(resource != null){
						CatalogNCXDecoder catalogNcxDecoder = new CatalogNCXDecoder();
						byte[] ncxData = resource.getData();
						//ncxData = EncryptUtils.decryptByAES(ncxData);
						if(XMLUtil.parserXml(catalogNcxDecoder, ncxData)){
							ArrayList<Catalog> catalogs = catalogNcxDecoder.getCatalogs();
							catalogHrefMap = catalogNcxDecoder.getCatalogHrefMap();
							Collections.sort(catalogs, new CatalogComparator());
							setCatalog(catalogs);
							ArrayList<String> chapterIds = new ArrayList<String>();
							for (Catalog catalog : catalogs) {
								Resource resources = manifestHrefResources.get(catalog.getHref());
								if(resources != null && !TextUtils.isEmpty(resources.getId())){
									chapterIds.add(resources.getId());
								}
							}
							setChapterIds(chapterIds);
						}
					}
				}
			}
			LogUtil.i("epub_book info: ", bookInfo.toString());
		}
	}
	
	private class CatalogComparator implements Comparator<Catalog> {
		@Override
		public int compare(Catalog object1, Catalog object2) {
			return object1.getIndex() - object2.getIndex();
		}
	}

	@Override
	public void recyle() {
		
	}

	@Override
	public Catalog getCatalogByIndex(int index) {
		String chapterID = getChapterIds().get(index);
		Resource resource = manifestIdResources.get(chapterID);
		if(resource != null && catalogHrefMap != null){
			return catalogHrefMap.get(resource.getHref());
		}
		return null;
	}

	@Override
	public int getChapterIndex(Catalog catalog) {
		Resource resource = manifestHrefResources.get(catalog.getHref());
		if(resource != null){
			ArrayList<String> chapterIds = getChapterIds();
			for (int i = 0;i < chapterIds.size();i++) {
				String id = chapterIds.get(i);
				if(id.equals(resource.getId())){
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public String getChapter(int chapterID) throws Exception {
		Resource resource = manifestIdResources.get(getChapterIds().get(chapterID));
		if(resource != null){
			return new String(resource.getData());
		}
		return null;
	}

	public Resource findResource(String path){
		Resource resource = null;
		if(!TextUtils.isEmpty(path)){
			if(manifestHrefResources != null){
				resource = manifestHrefResources.get(path);
				if(resource == null){//校验是否获取到资源，如果无法获取到资源，重新遍历资源MAP
					Set<String> keys = manifestHrefResources.keySet();
					for(String key : keys){
						if(key.endsWith(path) || path.endsWith(key)){
							resource = manifestHrefResources.get(key);
							break;
						}
					}
				}
				if(resource == null){
					Set<String> keys = allResources.keySet();
					for(String key : keys){
						if(key.endsWith(path) || path.endsWith(key)){
							resource = allResources.get(key);
							break;
						}
					}
				}
			}
		}
		return resource;
	}

	
	public String contentCover;

    public InputStream getCoverStream(){
    	if (TextUtils.isEmpty(contentCover)) {
		}else{  
            if(manifestIdResources.containsKey(contentCover)){
                Resource resource = manifestIdResources.get(contentCover);
                try {
                    return resource.getDataStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
        return null;
    }
}
