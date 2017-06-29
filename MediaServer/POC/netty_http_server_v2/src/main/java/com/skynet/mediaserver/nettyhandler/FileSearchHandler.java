package com.skynet.mediaserver.nettyhandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.skynet.mediaserver.MediaData;
import com.skynet.mediaserver.utils.FSMediaUtils;
import com.skynet.mediaserver.utils.HttpResponseUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

public class FileSearchHandler extends SimpleChannelInboundHandler<HttpObject>{
	protected String pathPrefix = "/search";
	private String baseFolder;
	private HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	private HttpPostRequestDecoder decoder; 
	
	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpRequest) {
		}else{
			return;
		}
		
		HttpRequest httpRequest = (HttpRequest) msg;
		String uri = httpRequest.uri();
		if (uri == null) {
			throw new Exception("Cannot got request URI");
		}
		if (!uri.startsWith(getPathPrefix())) {
			ctx.fireChannelRead(msg);
			return;
		}
		
		String searchKey = parseSearchKey(ctx, httpRequest);
		if (null == searchKey){
			HttpResponseStatus o;
			HttpResponseUtils.sendSimpleResponse(ctx, "Not support POST method", HttpResponseStatus.METHOD_NOT_ALLOWED, true, httpRequest);
			return;
		}
		
		List<MediaData> results = searchByKey(searchKey);
		responseSearchResult(results,ctx, httpRequest);
		
	}

	private void responseSearchResult(List<MediaData> results, ChannelHandlerContext ctx, HttpMessage httpRequest) {
		String text = new Gson().toJson(results);
		HttpResponseUtils.sendJsonResponse(ctx, text, HttpResponseStatus.OK, false, httpRequest);
	}

	private List<MediaData> searchByKey(String searchKey) throws Exception {
		List<File> files = new ArrayList<File>();
		searchFilesByKey(files, searchKey, new File(baseFolder));
		List<MediaData> results = new ArrayList();
		for(File file : files){
			results.add(FSMediaUtils.parseFileInfo(file, new File(baseFolder)));
		}
		return results;
	}

	private void searchFilesByKey(List<File> files, String searchKey, File file) {
		if (file.isFile()){
			if (file.getAbsolutePath().toLowerCase().contains(searchKey)){
				files.add(file);
			}
			return;
		}
		File[] subFiles = file.listFiles();
		for(File subFile: subFiles){
			searchFilesByKey(files, searchKey, subFile);
		}
	}

	private String parseSearchKey(ChannelHandlerContext ctx, HttpRequest httpRequest) {
		HttpMethod method = httpRequest.method();
		if (method.equals(HttpMethod.GET)){
			QueryStringDecoder queryDecoder = new QueryStringDecoder(httpRequest.uri());
			List<String> queryKey = queryDecoder.parameters().get("key");
			if (queryKey == null){
				return null;
			}
			return queryKey.get(0);
		}

		return null;
	}

}
