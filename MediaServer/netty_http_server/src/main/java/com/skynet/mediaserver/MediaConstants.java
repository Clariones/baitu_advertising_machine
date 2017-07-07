package com.skynet.mediaserver;

public class MediaConstants {
	// which app is using the media server. default is "all_public"
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_APPKEY = "appKey";
	public static final String DEFAULT_PARAM_APPKEY = "all_public";
	
	// is this picture need authorization or not
	public static final String PARAM_NEED_AUTH = "needAuth";
	public static final String DEFAULT_PARAM_NEED_AUTH = "false";
	
	// what kind of category this resource is 
	public static final String PARAM_CATEGORY = "category";
	public static final String DEFAULT_PARAM_CATEGORY = "all/any";
	
	// user assigned key for this resource. default is file original name
	public static final String PARAM_RESOURCE_KEY = "resourceKey";
	
	// comments for this resource. default is empty
	public static final String PARAM_RESOURCE_COMMENTS = "comments";
	
	
	public static final String PARAM_FILE_NAME = "fileName";
	public static final String PARAM_FILE_STREAM = "fileStream";
	public static final String PARAM_FILE = "fileInOS";
	public static final String PARAM_FILE_Size = "fileSize";
	
	public static final String PARAM_SEARCH_KEY = "key";
	public static final String PARAM_SIZE_WIDTH = "w";
	public static final String PARAM_SIZE_HEIGHT = "h";
	public static final String PARAM_SIZE_SCALE_TYPE = "scale";
	public static final String PARAM_DELETE_SCOPE = "delete";
	
	// Returned status of this resource operation
	public static final String RESULT_STATUS = "status";
	
	public static final String AJAX_REQUEST_POSTFIX=".ajax";
	public static final String FROM_REQUEST_POSTFIX=".html";
	
	// resize method
	public static final String RESIZE_METHOD_SCALE_TO_FIT = "fit";
	public static final String RESIZE_METHOD_SCALE_TO_FILL = "fill";
	public static final String DELETE_SCOPE_ALL = "all";
	public static final String DELETE_SCOPE_SELF = "self";
	public static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
}
