package com.skynet.mediaserver.nettyhandler;

import java.io.File;
import java.util.logging.Level;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;

public class HelpHandler extends BasicHttpRequestHandler {
	protected static final String REQUEST_PATH_PREFIX = "/help/";
	protected String baseFolder;
	
	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	@Override
	protected String getSelfUriPrefix() {
		return REQUEST_PATH_PREFIX;
	}
	

	@Override
	protected boolean isUriMatched(String uri) {
		if (uri.equals("/help")){
			return true;
		}
		return super.isUriMatched(uri);
	}

	protected boolean isValidMethod(HttpMethod method) {
		return method.equals(HttpMethod.GET);
	}
	
	@Override
	protected void onRequest(ChannelHandlerContext ctx) throws Exception {
		//writeStringResponse(ctx, HttpResponseStatus.OK, "Hello World!", "text/html; charset=UTF-8", true);
		String reqPath = calcRequestPath();
		File tgtFile = getFileRealPath(reqPath);
		logger().log(Level.INFO, "request for " + reqPath+"->"+tgtFile.getAbsolutePath());
		sendResponseFile(ctx, tgtFile);
	}

	private void sendResponseFile(ChannelHandlerContext ctx, File tgtFile) throws Exception {
		sendStaticFile(ctx, tgtFile, getRequest());
	}

	private File getFileRealPath(String reqPath) {
		return new File(baseFolder+File.separator+reqPath);
	}

	private String calcRequestPath() {
		String path = getRequest().uri();
		int pos = path.lastIndexOf('?');
		if (pos > 0){
			path = path.substring(0,  pos);
		}
		if (path.equals("/help") || path.equals("/help/")){
			return "index.html";
		}
		return path.substring(REQUEST_PATH_PREFIX.length());
	}

}
