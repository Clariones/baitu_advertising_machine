package com.skynet.mediaserver.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.skynet.mediaserver.MediaConstants;
import com.skynet.mediaserver.MediaData;

public class FSMediaUtils {

	public static Map<String, String> level1ContentType =  new HashMap<String, String>();
	public static Map<String, String> level2ContentType =  new HashMap<String, String>();
	static {
		level2ContentType.put(".gif", "image/gif");
		level2ContentType.put(".jpg", "image/jpeg");
		level2ContentType.put(".jpeg", "image/jpeg");
		level2ContentType.put(".png", "image/png");
	}
	
	public static final String BINARY_CONTENT_TYPE="application/octet-stream";
	private static String calcHashPath(String key){
		int hashCode = key.hashCode();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<4;i++){
			sb.append(hashCode & 0xFF).append('/');
			hashCode >>= 8;
		}
		return sb.toString();
	}
	private static String getPostfix(String filename){
		int pos = filename.lastIndexOf('.');
		if (pos < 0){
			return null;
		}
		return filename.substring(pos);
	}
	public static String makeKey(String appKey, MediaData media) {
		String key = media.getResourceKey();
		String fileName = media.getFileName();
		String keyPostfix = getPostfix(key);
		String filePostfix = getPostfix(fileName);
		if (keyPostfix == null && filePostfix != null){
			key += filePostfix;
		}
		
		key = key.replaceAll("[\\\\/\\*@]", " ");
		StringBuffer sb = new StringBuffer();
		if (media.isNeedAuth()){
			sb.append("authed/");
		}else{
			sb.append("public/");
		}
		sb.append(appKey).append('/').append(media.getCategory()).append('/').append(calcHashPath(key)).append(key);
		return sb.toString().replaceAll("/+", "/").replaceAll("\\s+", "_");
	}

	public static String calcContentTypeByName(String fileName){
		String filePost = fileName.toLowerCase();
		for(Entry<String, String> ent : level1ContentType.entrySet()){
			if (filePost.endsWith(ent.getKey())){
				return ent.getValue();
			}
		}
		for(Entry<String, String> ent : level2ContentType.entrySet()){
			if (filePost.endsWith(ent.getKey())){
				return ent.getValue();
			}
		}
		return BINARY_CONTENT_TYPE;
	}

	protected static final Pattern ptnFSMediaInfo = Pattern.compile("(\\w+)/(\\w+)/(.*?)(/\\d+){4}/([^/]+)");
	public static MediaData parseFileInfo(File file, File baseFolder) throws Exception {
		MediaData data = new MediaData();
		String filePath = file.getAbsoluteFile().getCanonicalPath();
		String basePath = baseFolder.getAbsoluteFile().getCanonicalPath();
		filePath = filePath.substring(basePath.length());
		if (filePath.startsWith("/")){
			filePath = filePath.substring(1);
		}
		Matcher m = ptnFSMediaInfo.matcher(filePath);
		m.matches();
		data.setCategory(m.group(3));
		data.setFileName(m.group(5));
		data.setNeedAuth(m.group(1).equals("authed"));
		data.setAppKey(m.group(2));
		data.setMediaUri(filePath);
		return data;
	}
}
