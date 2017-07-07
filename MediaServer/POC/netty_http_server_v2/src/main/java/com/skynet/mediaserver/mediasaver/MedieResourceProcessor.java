package com.skynet.mediaserver.mediasaver;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.skynet.mediaserver.MediaData;

public interface MedieResourceProcessor {

	String storeMedia(String appKey, MediaData media) throws Exception;
	String getBaseFolder();
	File getFile(String resourceUri, Map<String, Object> parameters);
	List<MediaData> search(Map<String, Object> parameters) throws Exception;
	int delete(String reqPath, Map<String, Object> parameters) throws Exception;

}
