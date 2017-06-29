package com.skynet.mediaserver.nettyhandler;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class NotFoundHandler extends SimpleChannelInboundHandler<HttpObject> {

	protected String pathPrefix = "/help";
	protected String baseFolder;

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
		} else {
			return;
		}

		HttpRequest req = (HttpRequest) msg;
		FullHttpResponse response;
		String msgText = req.uri() + " not found";
		System.out.println(msgText);
		ByteBuf buf = copiedBuffer(msgText, CharsetUtil.UTF_8);
		;
		// Build the response object.
		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, buf);

		response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
		response.headers().set(CONTENT_LENGTH, buf.readableBytes());
		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

		// Write the response.
		ctx.channel().writeAndFlush(response);
		ctx.close();
	}

}
