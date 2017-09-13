package com.skynet.adplayer.common;

import java.util.HashMap;
import java.util.Map;

public class SimpleAjaxResponse {
	protected boolean success;
	protected String message;
	protected Map<String, Object> data;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	
	public void markFail(String string) {
		setSuccess(false);
		setMessage(string);
	}
	public void addData(String key, Object value) {
		ensureData();
		data.put(key, value);
	}
	private void ensureData() {
		if (data == null){
			data = new HashMap<String, Object>();
		}
	}
	
	
}
