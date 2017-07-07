package com.skynet.mediaserver.nettyhandler;

import io.netty.channel.ChannelHandlerContext;

public class NotFoundHandler extends BasicHttpRequestHandler {

	@Override
	protected String getSelfUriPrefix() {
		return null;
	}
	
	@Override
	protected boolean isUriMatched(String uri) {
		return true; // this must be the last handler, just return 404
	}

	@Override
	protected void onRequest(ChannelHandlerContext ctx) throws Exception {
		sendNotFound(ctx, request.uri());
	}

	
}
