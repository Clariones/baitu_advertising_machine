package com.skynet.mediaserver.nettyhandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import com.google.gson.Gson;
import com.skynet.mediaserver.MediaConstants;
import com.skynet.mediaserver.MediaData;
import com.skynet.mediaserver.mediasaver.MedieResourceProcessor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class FileDeleteHandler extends BasicHttpRequestHandler{
	protected static final String REQUEST_PATH_PREFIX = "/delete/";
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
	protected void onRequest(ChannelHandlerContext ctx) throws Exception {
		String reqPath = calcRequestPath();
		int results = getMediaSaver().delete(reqPath, getParameters());
		super.writeStringResponse(ctx, HttpResponseStatus.OK, String.valueOf(results), MediaConstants.CONTENT_TYPE_JSON, true);
	}
	private String calcRequestPath() throws UnsupportedEncodingException {
		String path = getRequest().uri();
		path = URLDecoder.decode(path, "UTF-8");
		int pos = path.lastIndexOf('?');
		if (pos > 0){
			path = path.substring(0,  pos);
		}
		path = path.substring(REQUEST_PATH_PREFIX.length());
		return path;
	}
	
}
