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

	/**
	 * create a string used for calc MD5, so not all fields were used, and not well formated
	 * @return
     */
	public String toStringForMD5(){
		StringBuilder sb = new StringBuilder();
		if (getAdMachine() != null) {
			sb.append("admachine:").append(getAdMachine().getId()).append(";");
		}
		if (getPages() != null){
			for(AdMachinePageContent page : getPages()){
				sb.append(page.getContentType()).append(page.getContentId()).append(',');
				sb.append(page.getStartTimeSec()).append("-").append(page.getEndTimeSec()).append(',');
				if (page.getPlayDuration() > 0){
					sb.append("playduration:").append(page.getPlayDuration()).append(',');
				}
				if (page.getImageUri() != null) {
					sb.append("imageurl:").append(page.getImageUri()).append(',');
				}
				sb.append("\n");
			}
		}

		return sb.toString();
	}
}
