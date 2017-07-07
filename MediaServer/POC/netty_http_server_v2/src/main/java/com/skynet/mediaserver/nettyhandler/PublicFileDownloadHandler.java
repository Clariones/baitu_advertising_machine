package com.skynet.mediaserver.nettyhandler;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;

import com.skynet.mediaserver.mediasaver.MedieResourceProcessor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;

public class PublicFileDownloadHandler extends BasicHttpRequestHandler {
	protected static final String REQUEST_PATH_PREFIX = "/public/";
	protected MedieResourceProcessor mediaSaver;

	public MedieResourceProcessor getMediaSaver() {
		return mediaSaver;
	}


	public void setMediaSaver(MedieResourceProcessor mediaSaver) {
		this.mediaSaver = mediaSaver;
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
		File tgtFile = getMediaSaver().getFile(reqPath, getParameters());
		if (tgtFile == null){
			super.sendNotFound(ctx, getRequest().uri());
			return;
		}
		sendStaticFile(ctx, tgtFile, getRequest());
	}

	private String calcRequestPath() throws UnsupportedEncodingException {
		String path = getRequest().uri();
		path = URLDecoder.decode(path, "UTF-8");
		int pos = path.lastIndexOf('?');
		if (pos > 0){
			path = path.substring(0,  pos);
		}
		return path;
	}
	
}
