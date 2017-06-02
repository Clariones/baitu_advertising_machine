package com.skynet.mediaserver;

public interface MedieResourceSaver {

	String storeMedia(String appKey, MediaData media) throws Exception;
	String getBaseFolder();

}
