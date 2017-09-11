package com.skynet.adplayer.common;

import java.util.Date;

public class AdMachinePageContent {
	protected int playDuration;
	protected String imageUri;
	protected String contentId;
	protected String contentType;
	protected Date startDate;
	protected Date endDate;
	protected int startTimeSec;
	protected int endTimeSec;
	protected String title;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	protected int index;
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getPlayDuration() {
		return playDuration;
	}
	public void setPlayDuration(int playDuration) {
		this.playDuration = playDuration;
	}
	public String getImageUri() {
		return imageUri;
	}
	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public int getStartTimeSec() {
		return startTimeSec;
	}
	public void setStartTimeSec(int startTimeSec) {
		this.startTimeSec = startTimeSec;
	}
	public int getEndTimeSec() {
		return endTimeSec;
	}
	public void setEndTimeSec(int endTimeSec) {
		this.endTimeSec = endTimeSec;
	}
	
}
