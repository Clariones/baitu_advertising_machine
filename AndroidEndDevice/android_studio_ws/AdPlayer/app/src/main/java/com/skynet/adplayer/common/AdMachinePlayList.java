package com.skynet.adplayer.common;

import java.util.List;

public class AdMachinePlayList {
	protected long respTimestamp;
	protected String errMessage;
	protected List<AdMachinePageContent> pages;
	protected boolean preview;
	protected boolean success;
	protected Refrigerator adMachine;
	public long getRespTimestamp() {
		return respTimestamp;
	}
	public void setRespTimestamp(long respTimestamp) {
		this.respTimestamp = respTimestamp;
	}
	public String getErrMessage() {
		return errMessage;
	}
	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}
	public List<AdMachinePageContent> getPages() {
		return pages;
	}
	public void setPages(List<AdMachinePageContent> pages) {
		this.pages = pages;
	}
	public boolean isPreview() {
		return preview;
	}
	public void setPreview(boolean preview) {
		this.preview = preview;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public Refrigerator getAdMachine() {
		return adMachine;
	}
	public void setAdMachine(Refrigerator adMachine) {
		this.adMachine = adMachine;
	}
	
	
}
