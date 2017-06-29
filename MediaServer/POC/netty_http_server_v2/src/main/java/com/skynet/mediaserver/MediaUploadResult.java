package com.skynet.mediaserver;

import java.util.List;

public class MediaUploadResult {
	protected String status;
	protected List<String> resourceUris;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<String> getResourceUris() {
		return resourceUris;
	}
	public void setResourceUris(List<String> resourceUris) {
		this.resourceUris = resourceUris;
	}
	
	
}
