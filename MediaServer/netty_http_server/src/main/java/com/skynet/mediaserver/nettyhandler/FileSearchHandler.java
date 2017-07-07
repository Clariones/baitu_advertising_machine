package com.skynet.mediaserver.nettyhandler;

import java.util.List;

import com.google.gson.Gson;
import com.skynet.mediaserver.MediaData;
import com.skynet.mediaserver.mediasaver.MedieResourceProcessor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class FileSearchHandler extends BasicHttpRequestHandler{
	protected static final String REQUEST_PATH_PREFIX = "/search";
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
		List<MediaData> results = getMediaSaver().search(getParameters());
		String text = new Gson().toJson(results);
		super.writeStringResponse(ctx, HttpResponseStatus.OK, text, "application/json; charset=utf-8", true);
	}
	
}
