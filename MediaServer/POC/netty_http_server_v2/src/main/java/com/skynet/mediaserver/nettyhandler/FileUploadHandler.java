package com.skynet.mediaserver.nettyhandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skynet.mediaserver.MediaConstants;
import com.skynet.mediaserver.MediaData;
import com.skynet.mediaserver.MediaUploadInfo;
import com.skynet.mediaserver.MediaUploadResult;
import com.skynet.mediaserver.mediasaver.MedieResourceProcessor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;

public class FileUploadHandler extends BasicHttpRequestHandler {
	protected static final Pattern ptnUri = Pattern.compile("/upload((\\.ajax)|(\\.html))([/\\?].+)?");
	protected static final String REQUEST_PATH_PREFIX = "/upload/";
	
	protected MedieResourceProcessor mediaSaver;
	protected static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	static {
		DiskFileUpload.deleteOnExitTemporaryFile = false;
		DiskFileUpload.baseDirectory = "/tmp";
	}

	protected MediaUploadInfo cleanUpAjaxInput() throws IOException {
		MediaUploadInfo uploadInfo = createMediaDataFromJson();
		mergeWithFileData(uploadInfo);
		return uploadInfo;
	}
	
	protected MediaUploadInfo cleanUpFormInput() throws IOException {
		MediaUploadInfo uploadInfo = createMediaDataFromForm();
		mergeWithFileData(uploadInfo);
		return uploadInfo;
	}
	private MediaUploadInfo createMediaDataFromForm() {
		MediaUploadInfo info = new MediaUploadInfo();
		Object appKeyList = getParameters().remove(MediaConstants.PARAM_APPKEY);
		if (appKeyList == null || !(appKeyList instanceof List) || ((List)appKeyList).isEmpty()){
			info.setAppKey(MediaConstants.DEFAULT_PARAM_APPKEY);
		}else{
			info.setAppKey(((List<String>)appKeyList).get(0));
		}
		List<MediaData> medias = ensureMedias(info);
		for(String key : getParameters().keySet()){
			String value = getParameterString(key);
			int idx = getFileIndex(key);
			String keyName = key.substring(0, key.indexOf('['));
			MediaData mData = ensureMediaData(medias, idx);
			if (MediaConstants.PARAM_CATEGORY.equals(keyName)){
				mData.setCategory(value);
			}else if (MediaConstants.PARAM_NEED_AUTH.equals(keyName)){
				mData.setNeedAuth(Boolean.parseBoolean(value));
			}else if (MediaConstants.PARAM_RESOURCE_KEY.equals(keyName)){
				mData.setResourceKey(value);
			}else if (MediaConstants.PARAM_RESOURCE_COMMENTS.equals(keyName)){
				mData.setComments(value);
			}else {
				// nothing to do.
			}
			
		}
		return info;
	}

	private String getParameterString(String key) {
		Object inList = getParameters().get(key);
		if (inList == null || !(inList instanceof List) || ((List)inList).isEmpty()){
			return null;
		}else{
			return (((List<String>)inList).get(0));
		}
	}

	protected MediaUploadInfo createMediaDataFromJson() {
		
		Object inputParam = this.getParameters().get("jsonstr");
		if (!(inputParam instanceof List)){
			return new MediaUploadInfo();
		}
		List<String> inputList = (List<String>) inputParam;
		if (inputList == null || inputList.isEmpty()){
			return new MediaUploadInfo();
		}
		String jsonStr = inputList.get(0);
		Gson gson = new Gson();
		return gson.fromJson(jsonStr, MediaUploadInfo.class);
	}


	protected void dumpInput() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String params = gson.toJson(this.getParameters());
		System.out.println("Parameters: " + params);
		System.out.println("Headers: " + gson.toJson(this.getCookies()));
		if (this.getFileDatas() == null){
			System.out.println("===>No any file uploaded");
			return;
		}
		for(FileUpload fileData : this.getFileDatas()){
			System.out.printf("===>%s,%s,%s,%s\n", fileData.getName(), fileData.isInMemory()?"Memory":"Disk",
					fileData.getFilename(), fileData.isInMemory()?"null":fileData.getFile());
		}
		
	}
	

	protected void dumpUploadedData(MediaUploadInfo uploaded) {
		if (uploaded == null){
			System.out.println("uploaded is null");
			return;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String params = gson.toJson(uploaded);
		System.out.println("uploaded: " + params);
	}


	@Override
	protected void ensureMembers() {
		DiskFileUpload.baseDirectory = mediaSaver.getBaseFolder();
		super.ensureMembers();
	}

	protected int getFileIndex(String name) {
		int pos1 = name.indexOf('[');
		int pos2 = name.indexOf(']');
		return Integer.parseInt(name.substring(pos1+1, pos2));
	}


	public MedieResourceProcessor getMediaSaver() {
		return mediaSaver;
	}


	@Override
	protected String getSelfUriPrefix() {
		return REQUEST_PATH_PREFIX;
	}


	@Override
	protected boolean isUriMatched(String uri) {
		Matcher m = ptnUri.matcher(uri);
		return m.matches();
	}


	@Override
	protected boolean isValidMethod(HttpMethod method) {
		return HttpMethod.POST.equals(method);
	}


	protected void mergeWithFileData(MediaUploadInfo uploadInfo) throws IOException {
		if (getFileDatas() == null){
			uploadInfo.setMedias(null);
			return;
		}
		// ensure medias member
		List<MediaData> mediaFromInfo = ensureMedias(uploadInfo);
		// app-key first
		if (uploadInfo.getAppKey() == null){
			uploadInfo.setAppKey(MediaConstants.DEFAULT_PARAM_APPKEY);
		}
		for(FileUpload fData : getFileDatas()){
			int idx = getFileIndex(fData.getName());
			// create empty data for default values if needed
			MediaData mData = ensureMediaData(mediaFromInfo, idx);
			mData.setAppKey(uploadInfo.getAppKey());
			if (mData.getCategory() == null){
				mData.setCategory(MediaConstants.DEFAULT_PARAM_CATEGORY);
			}
			if (mData.getFileName() == null){
				mData.setFileName(fData.getFilename());
			}
			if (mData.getComments() == null){
				mData.setComments(mData.getFileName());
			}
			if (mData.getResourceKey() == null){
				mData.setResourceKey(mData.getFileName());
			}
			if (fData.isInMemory()){
				if (fData.get().length > 0){
					mData.setContentStream(new ByteArrayInputStream(fData.get()));
				}else{
					mData.setContentStream(null);
				}
				mData.setContentFile(null);
			}else{
				mData.setContentFile(fData.getFile());
				mData.setContentStream(null);
			}
		}
		
		Iterator<MediaData> it = mediaFromInfo.iterator();
		while(it.hasNext()){
			MediaData data = it.next();
			if (data.getContentFile() == null && data.getContentStream() == null){
				it.remove();
			}
		}
	}

	private MediaData ensureMediaData(List<MediaData> mediaFromInfo, int idx) {
		if (idx >= mediaFromInfo.size()){
			for(int i=mediaFromInfo.size(); i<=idx;i++){
				mediaFromInfo.add(new MediaData());
			}
		}
		MediaData mData = mediaFromInfo.get(idx);
		return mData;
	}

	private List<MediaData> ensureMedias(MediaUploadInfo uploadInfo) {
		List<MediaData> mediaFromInfo = uploadInfo.getMedias();
		if (mediaFromInfo == null){
			mediaFromInfo = new ArrayList<MediaData>();
			uploadInfo.setMedias(mediaFromInfo);
		}
		return mediaFromInfo;
	}


	@Override
	protected void onRequest(ChannelHandlerContext ctx) throws Exception {
		dumpInput();
		MediaUploadInfo uploaded = null;
		if (request.uri().startsWith("/upload.ajax")){
			uploaded = cleanUpAjaxInput();
		}else{
			uploaded = cleanUpFormInput();
		}
		dumpUploadedData(uploaded);
		if (uploaded.getMedias() == null || uploaded.getMedias().isEmpty()){
			super.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		List<String> result = new ArrayList<String>();
		for(MediaData media : uploaded.getMedias()){
			String srcUri = getMediaSaver().storeMedia(uploaded.getAppKey(), media);
			if (srcUri != null){
				result.add(srcUri);
			}
		}
		MediaUploadResult uploadResponse = new MediaUploadResult();
		if (result.isEmpty()){
			uploadResponse.setStatus("no any file uploaded");
			uploadResponse.setResourceUris(result);
		}else{
			uploadResponse.setStatus("success");
			uploadResponse.setResourceUris(result);
		}
		
		String respStr = new Gson().toJson(uploadResponse);
		String respType = MediaConstants.CONTENT_TYPE_JSON;
		super.writeStringResponse(ctx, HttpResponseStatus.OK, respStr, respType, true);
	}


	public void setMediaSaver(MedieResourceProcessor mediaSaver) {
		this.mediaSaver = mediaSaver;
	}
	
}
