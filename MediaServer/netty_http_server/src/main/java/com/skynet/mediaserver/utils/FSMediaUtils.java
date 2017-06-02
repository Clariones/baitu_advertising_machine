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
	
	public static String makeKey(String appKey, MediaData media) {
		StringBuffer sb = new StringBuffer();
		if (media.isNeedAuth()){
			sb.append("authed/");
		}else{
			sb.append("public/");
		}
		sb.append(appKey).append('/').append(media.getCategory()).append('/').append(media.getFileName());
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

	protected static final Pattern ptnFSMediaInfo = Pattern.compile("(\\w+)/(\\w+)/(.*?)/([^/]+)");
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
		data.setFileName(m.group(4));
		data.setNeedAuth(m.group(1).equals("authed"));
		data.setAppKey(m.group(2));
		data.setMediaUri(filePath);
		return data;
	}
}
