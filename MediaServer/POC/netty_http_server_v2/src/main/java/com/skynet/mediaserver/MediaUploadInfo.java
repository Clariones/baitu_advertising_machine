package com.skynet.mediaserver;

import java.util.List;

public class MediaUploadInfo {
	protected String appKey = MediaConstants.DEFAULT_PARAM_APPKEY;
	protected List<MediaData> medias;
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public List<MediaData> getMedias() {
		return medias;
	}
	public void setMedias(List<MediaData> medias) {
		this.medias = medias;
	}
	
	
}
