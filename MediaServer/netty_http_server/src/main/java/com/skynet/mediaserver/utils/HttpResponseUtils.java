package com.skynet.mediaserver.utils;

import static io.netty.buffer.Unpooled.copiedBuffer;

import java.util.Collections;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

public class HttpResponseUtils {
	public static void sendSimpleResponse(ChannelHandlerContext ctx, String msg, HttpResponseStatus status, boolean forceClose, HttpMessage request) {
		sendResponse(ctx, msg, status, forceClose, request, "text/html; charset=UTF-8");
	}
	public static void sendJsonResponse(ChannelHandlerContext ctx, String msg, HttpResponseStatus status, boolean forceClose, HttpMessage request) {
		sendResponse(ctx, msg, status, forceClose, request, "application/json; charset=UTF-8");
	}

	private static void sendResponse(ChannelHandlerContext ctx, String msg, HttpResponseStatus status,
			boolean forceClose, HttpMessage request, String contentType) {
		ByteBuf buf = copiedBuffer(msg, CharsetUtil.UTF_8);
		
		
		// Decide whether to close the connection or not.
		boolean close = forceClose
				|| HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(request.headers().get(HttpHeaderNames.CONNECTION))
				|| request.protocolVersion().equals(HttpVersion.HTTP_1_0) && !HttpHeaderValues.KEEP_ALIVE
						.contentEqualsIgnoreCase(request.headers().get(HttpHeaderNames.CONNECTION));

        // Build the response object.
		FullHttpResponse response;
		// Build the response object.
		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);

		response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
		ctx.channel().writeAndFlush(response);


        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        }

        Set<io.netty.handler.codec.http.cookie.Cookie> cookies;
        String value = request.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.LAX.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));
            }
        }
        // Write the response.
        ChannelFuture future = ctx.channel().writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
	}
}
