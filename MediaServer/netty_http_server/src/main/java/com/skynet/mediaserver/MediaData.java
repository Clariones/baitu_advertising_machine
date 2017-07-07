package com.skynet.mediaserver;

import java.io.File;
import java.io.InputStream;

public class MediaData {
	protected String category = MediaConstants.DEFAULT_PARAM_CATEGORY;
	protected boolean needAuth = Boolean.valueOf(MediaConstants.DEFAULT_PARAM_NEED_AUTH);
	protected String resourceKey = null;
	protected String comments= null;
	protected String fileName= null;
	protected transient InputStream contentStream;
	protected File contentFile;
	protected String appKey;
	protected String mediaUri;
	public String getMediaUri() {
		return mediaUri;
	}
	public void setMediaUri(String mediaUri) {
		this.mediaUri = mediaUri;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public File getContentFile() {
		return contentFile;
	}
	public void setContentFile(File contentFile) {
		this.contentFile = contentFile;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public boolean isNeedAuth() {
		return needAuth;
	}
	public void setNeedAuth(boolean needAuth) {
		this.needAuth = needAuth;
	}
	public String getResourceKey() {
		return resourceKey;
	}
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public InputStream getContentStream() {
		return contentStream;
	}
	public void setContentStream(InputStream contentStream) {
		this.contentStream = contentStream;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
