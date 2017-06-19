package com.skynet.mediaserver.nettyhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.skynet.mediaserver.MediaConstants;
import com.skynet.mediaserver.MediaData;
import com.skynet.mediaserver.MediaUploadInfo;
import com.skynet.mediaserver.MediaUploadResult;
import com.skynet.mediaserver.MedieResourceSaver;
import com.skynet.mediaserver.mediasaver.SimpleFileSystemMediaSaver;
import com.skynet.mediaserver.utils.HttpResponseUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class FileUploadHandler extends SimpleChannelInboundHandler<HttpObject> {
	protected Logger logger = Logger.getLogger(FileUploadHandler.class.getName());

	protected boolean inWorking = false;
	protected String pathPrefix = "/upload";
	protected HttpRequest request;
	protected Map<String, String> requestHeaders;
	protected List<FileUpload> fileDatas;
	protected MediaUploadInfo requestParameters;
	private HttpPostRequestDecoder decoder;
	protected String reqParamType = "form";
	protected MedieResourceSaver mediaSaver = new SimpleFileSystemMediaSaver();
	
	public MedieResourceSaver getMediaSaver() {
		return mediaSaver;
	}

	public void setMediaSaver(MedieResourceSaver mediaSaver) {
		this.mediaSaver = mediaSaver;
	}

	private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	static {
		DiskFileUpload.deleteOnExitTemporaryFile = false;
		DiskFileUpload.baseDirectory = "/udisk/tmp";
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public HttpRequest getRequest() {
		return request;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		DiskFileUpload.baseDirectory = mediaSaver.getBaseFolder();
		if (msg instanceof HttpRequest){
			System.out.println("Got request " + ((HttpRequest) msg).uri());
		}
		if (!isFileUploadRequest(ctx, msg)) {
			ctx.fireChannelRead(msg);
			return;
		}
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		}else if (msg instanceof HttpContent){
			handleHttpContent(ctx, (HttpContent) msg);
		}

	}

	private void handleHttpContent(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
		if (decoder == null){
			return;
		}
		// New chunk is received
        HttpContent chunk = msg;
        try {
            decoder.offer(chunk);
            readHttpDataByChunk();
        } catch (Exception e1) {
            e1.printStackTrace();
            responseException(ctx, e1);
            reset();
            return;
        }


        // example of reading only if at the end
        if (chunk instanceof LastHttpContent) {
        	if (!verifyRequestParameters()){
        		responseInvalidParameters(ctx);
        	}else{
        		long t1 = System.currentTimeMillis();
        		beforeProcessFileUploaded();
        		List<String> result = saveFileUploaded();
        		afterProcessFileUploaded();
        		long t2 = System.currentTimeMillis();
//        		System.out.println("Used time: " + (t2-t1)/1000.0);
        		sendSuccessResponse(ctx, result);
        		long t3 = System.currentTimeMillis();
//        		System.out.println("Used time: " + (t3-t1)/1000.0);
//        		System.out.println("??"+(System.currentTimeMillis() % 100000)/1000.0);
        	}
            reset();
        }
	}

	private void afterProcessFileUploaded() {
		// TODO Auto-generated method stub
		
	}

	private List<String> saveFileUploaded() throws Exception {
		String appKey = requestParameters.getAppKey();
		List<MediaData> medias = requestParameters.getMedias();
		List<String> results = new ArrayList<String>();
		for(MediaData media : medias){
			String key = mediaSaver.storeMedia(appKey, media);
			if (key == null){
				continue;
			}
			results.add(key);
		}
		return results;
	}

	/**
	 * set default values
	 */
	private void beforeProcessFileUploaded() {
		List<MediaData> list = this.requestParameters.getMedias();
		for(MediaData data : list){
			if (data.getResourceKey() == null || data.getResourceKey().isEmpty()){
				data.setResourceKey(data.getFileName());
			}
		}
		
	}

	private void responseInvalidParameters(ChannelHandlerContext ctx) {
		HttpResponseUtils.sendSimpleResponse(ctx, "Invalid parameters " + request.method().name(), HttpResponseStatus.METHOD_NOT_ALLOWED, true, request);
	}

	private boolean verifyRequestParameters() {
		// TODO Auto-generated method stub
		return true;
	}

	private void readHttpDataByChunk() throws Exception {
		double t1 = (System.currentTimeMillis() % 100000)/1000.0;
		boolean handled = false;
		while (decoder.hasNext()) {
			handled = true;
//			System.out.println("*"+(System.currentTimeMillis() % 100000)/1000.0);
			InterfaceHttpData data = decoder.next();
			if (data != null) {
				try {
					// new value
					processHttpData(data);
				} finally {
					if (data instanceof FileUpload){
						fileDatas.add((FileUpload) data);
					}else{
						data.release();
					}
				}
			}
		}
		if (!handled){
//			System.out.println("." + t1);
		}
	}

    private void processHttpData(InterfaceHttpData data) throws Exception {
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value = attribute.getValue();
            addReqParam(attribute.getName(), value);
        } else {
//        	System.out.println("$"+(System.currentTimeMillis() % 100000)/1000.0);
            if (data.getHttpDataType() == HttpDataType.FileUpload) {
//            	System.out.println(data.getClass().getCanonicalName());
                FileUpload fileUpload = (FileUpload) data;

                if (fileUpload.isInMemory()){
                	System.out.println("file " + fileUpload.getFilename()+": "+fileUpload.get().length+"(Bytes) uploaded into memory");
                }else{
                	System.out.println("file " + fileUpload.getFilename() +": " + fileUpload.getFile().length()+"(Bytes) uploaded.");
                }

                if (fileUpload.isCompleted()) {
                    processOneUploadedFile(fileUpload);
                } else {
                    throw new Exception("\tFile to be continued but should not!\r\n");
                }
            }
//            System.out.println("?"+(System.currentTimeMillis() % 100000)/1000.0);
        }
    }
    
    private static final Pattern ptnName = Pattern.compile("(\\w+)(\\[(\\d+)\\])?");
	private void addReqParam(String name, String value) {
		if (value == null || value.isEmpty()){
			return;
		}
		if (this.reqParamType.equals(MediaConstants.AJAX_REQUEST_POSTFIX) && name.equals("jsonstr")){
			Map<String, Object> reqParams = new Gson().fromJson(value, Map.class);
			addSingleParam(0, MediaConstants.PARAM_APPKEY, reqParams.get(MediaConstants.PARAM_APPKEY));
			List<Map<String, Object>> reqData = (List<Map<String, Object>>) reqParams.get("medias");
			if (reqData == null || reqData.isEmpty()){
				return;
			}
			for(int i=0;i<reqData.size();i++){
				Map<String, Object> data = reqData.get(i);
				for(Entry<String, Object> ent : data.entrySet()){
					addSingleParam(i, ent.getKey(), ent.getValue());
				}
			}
			return;
		}
		
		Matcher m = ptnName.matcher(name);
//		System.out.println("parse name " + name);
		if (!m.matches()){
			System.out.println( name + " not valid");
			return;
		}
		if (m.group(2) != null){
			addSingleParam(Integer.valueOf(m.group(3)), m.group(1), value);
		}else{
			addSingleParam(0, m.group(1), value);
		}
	}

	private void addSingleParam(int i, String name, Object value) {
		System.out.println("Add param " + name +"."+i+"="+value);
		if (name.equals(MediaConstants.PARAM_APPKEY)){
			requestParameters.setAppKey((String) value);
			return;
		}
		List<MediaData> reqDataList = requestParameters.getMedias();
		if (reqDataList == null){
			reqDataList = new ArrayList<MediaData>();
			requestParameters.setMedias(reqDataList);
		}
		for(int x=reqDataList.size();x<=i;x++){
			reqDataList.add(new MediaData());
		}
		MediaData data = reqDataList.get(i);
		if (MediaConstants.PARAM_CATEGORY.equals(name)){
			data.setCategory((String) value);
		}else if (MediaConstants.PARAM_NEED_AUTH.equals(name)){
			if (value instanceof Boolean){
				data.setNeedAuth((Boolean) value);
			}else{
				data.setNeedAuth(Boolean.valueOf((String) value));
			}
		}else if (MediaConstants.PARAM_RESOURCE_KEY.equals(name)){
			data.setResourceKey((String) value);
		}else if (MediaConstants.PARAM_RESOURCE_COMMENTS.equals(name)){
			data.setComments((String) value);
		}else if (MediaConstants.PARAM_FILE_NAME.equals(name)){
			data.setFileName((String) value);
		}else if (MediaConstants.PARAM_FILE_STREAM.equals(name)){
			data.setContentStream((InputStream) value);
		}else if (MediaConstants.PARAM_FILE.equals(name)){
			data.setContentFile((File) value);
		}else {
			System.out.println("Unsupported param " + name);
		}
	}

	private void processOneUploadedFile(FileUpload fileUpload) throws IOException {
		long t1=System.currentTimeMillis();
		String filePrefix = fileUpload.getName();
		addFileUpload(fileUpload);
		long t2=System.currentTimeMillis();
		System.out.println("processOneUploadedFile(): " + (t2-t1)/1000.0);
	}

	private void addFileUpload(FileUpload fileUpload) throws IOException {
		String name = fileUpload.getName();
		Matcher m = ptnName.matcher(name);
		if (!m.matches()){
			System.out.println("Invalid file element name " + name);
			return;
		}
		int i = 0;
		if (m.group(2) != null){
			i = Integer.parseInt(m.group(3));
		}
		addSingleParam(i, MediaConstants.PARAM_FILE_NAME, fileUpload.getFilename());
		if (fileUpload.isInMemory()){
			ByteArrayInputStream bins = new ByteArrayInputStream(fileUpload.get());
			addSingleParam(i, MediaConstants.PARAM_FILE_STREAM, bins);
		}else{
			System.out.println("FIle are in " + fileUpload.getFile().getAbsolutePath());
//			FileInputStream  fins = new FileInputStream(fileUpload.getFile());
//			addSingleParam(i, MediaConstants.PARAM_FILE_STREAM, fins);
			addSingleParam(i, MediaConstants.PARAM_FILE, fileUpload.getFile());
		}
	}

	private void sendSuccessResponse(ChannelHandlerContext ctx, List<String> keys) {
		//this.sendSimpleResponse(ctx, new Gson().toJson(requestParameters), HttpResponseStatus.OK, false);
		MediaUploadResult result = new MediaUploadResult();
		result.setStatus("success");
		result.setResourceUris(keys);
		String text = new Gson().toJson(result);
		HttpResponseUtils.sendSimpleResponse(ctx, text, HttpResponseStatus.OK, false, request);
	}

	private boolean isFileUploadRequest(ChannelHandlerContext ctx, HttpObject msg) {
		if (request == null) {
			return true;
		}
		return inWorking;
	}

	protected void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception {
		String uri = httpRequest.uri();
		if (uri == null) {
			throw new Exception("Cannot got request URI");
		}
		if (!uri.startsWith(getPathPrefix())) {
			inWorking = false;
			ctx.fireChannelRead(httpRequest);
			return;
		}
		inWorking = true;
		request = httpRequest;
		if (uri.endsWith(MediaConstants.AJAX_REQUEST_POSTFIX)){
			reqParamType = MediaConstants.AJAX_REQUEST_POSTFIX;
		}else{
			reqParamType = MediaConstants.FROM_REQUEST_POSTFIX;
		}

		saveHeaders();
		
		HttpMethod method = request.method();
		if (HttpMethod.GET.equals(method)) {
			// upload file must be POST
			responseNotSupportedMethod(ctx);
			reset();
			return;
		}
		
		try {
			fileDatas = new ArrayList<FileUpload>();
			requestParameters = new MediaUploadInfo();
            decoder = new HttpPostRequestDecoder(factory, request);
        } catch (Exception e1) {
            e1.printStackTrace();
            responseException(ctx, e1);
            reset();
            return;
        }
	}

	private void responseException(ChannelHandlerContext ctx, Exception e) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(out);
		e.printStackTrace(ps);
		String msg = out.toString();
		System.err.println(msg);
		HttpResponseUtils.sendSimpleResponse(ctx, "<pre>" + msg +"</pre>", HttpResponseStatus.INTERNAL_SERVER_ERROR, true, request);
	}

	private void reset() {
		inWorking = false;
		request = null;
		decoder = null;
		if (fileDatas != null){
			for(FileUpload data : fileDatas){
				data.release();
			}
			fileDatas.clear();
			fileDatas = null;
		}
		if (requestParameters != null){
			relaseRequestResources();
			requestParameters = null;
		}
		if (requestHeaders != null){
			requestHeaders.clear();
			requestHeaders = null;
		}
	}

	private void relaseRequestResources() {
		List<MediaData> datas = requestParameters.getMedias();
		if (datas == null) {
			return;
		}
		for (MediaData data : datas) {
			InputStream ins = data.getContentStream();
			if (ins == null) {
				continue;
			}
			try {
				ins.close();
				System.out.println("Close "+ data.getFileName());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void responseNotSupportedMethod(ChannelHandlerContext ctx) {
		HttpResponseUtils.sendSimpleResponse(ctx, "Do not support method " + request.method().name(), HttpResponseStatus.METHOD_NOT_ALLOWED, true, request);
	}

	

	private void saveHeaders() {
		HttpHeaders headers = request.headers();
		if (headers == null) {
			return;
		}
		Iterator<Entry<String, String>> it = headers.iteratorAsString();
		if (it == null) {
			return;
		}
		requestHeaders = new HashMap<String, String>();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			requestHeaders.put(entry.getKey(), entry.getValue());
		}
	}

}
