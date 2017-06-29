package com.skynet.mediaserver.nettyhandler;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public abstract class BasicHttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> {
	protected static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	protected Set<Cookie> cookies;
	protected HttpPostRequestDecoder decoder;
	protected HashMap<String, String> headers;
	protected Logger myLogger;
	protected Map<String, Object> parameters;
	protected HttpRequest request;
	protected List<FileUpload> fileDatas;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (!isMyRequest(ctx, msg)) {
			ctx.fireChannelRead(msg);
			return;
		}
		logger().log(Level.FINE, "handle request {0}/{1}", new Object[] { request.uri(), 2 });
		ensureColletionMembers();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof HttpContent) {
			handleHttpContent(ctx, (HttpContent) msg);
		}
	}

	private void ensureColletionMembers() {
		if (parameters == null) {
			parameters = new HashMap<String, Object>();
		}
		if (fileDatas == null) {
			fileDatas = new ArrayList<FileUpload>();
		}
	}

	protected abstract String getSelfUriPrefix();

	protected void handleHttpContent(ChannelHandlerContext ctx, HttpContent msg) {
		if (decoder == null) {
			return;
		}
		HttpContent chunk = msg;
		try {
			decoder.offer(chunk);
			readHttpDataByChunk();
			if (chunk instanceof LastHttpContent) {
				onPostRequestRecieved(ctx, msg);
				reset();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			responseException(ctx, e1);
			reset();
			return;
		}
	}

	protected abstract void onRequest(ChannelHandlerContext ctx) throws Exception;

	protected void onPostRequestRecieved(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
		onRequest(ctx);
	}

	protected void responseException(ChannelHandlerContext ctx, Exception e1) {
		// TODO Auto-generated method stub

	}

	protected void readHttpDataByChunk() throws Exception {
		while (decoder.hasNext()) {
			InterfaceHttpData data = decoder.next();
			if (data == null) {
				continue;
			}
			try {
				if (data.getHttpDataType() == HttpDataType.Attribute) {
					Attribute attribute = (Attribute) data;
					onHttpAttribute(attribute);
				} else if (data.getHttpDataType() == HttpDataType.FileUpload) {
					onHttpFileUpload((FileUpload) data);
				} else {
					onOtherHttpData(data);
				}
			} finally {
				if (!(data instanceof FileUpload)) {
					data.release();
				}
			}
		}
	}

	protected void onOtherHttpData(InterfaceHttpData data) throws Exception {
		throw new Exception("You must handle the " + data.getClass().getCanonicalName() + " yourself");
	}

	protected void onHttpFileUpload(FileUpload data) {
		fileDatas.add((FileUpload) data);
	}

	protected void onHttpAttribute(Attribute attribute) throws Exception {
		addReqParam(attribute.getName(), attribute.getValue());
	}

	protected void addReqParam(String name, final String value) throws Exception {
		Object oldValue = parameters.get(name);
		if (oldValue == null) {
			List<String> values = new ArrayList<String>();
			values.add(value);
			parameters.put(name, (Object) values);
			return;
		}
		if (oldValue instanceof Collection) {
			Collection<Object> values = (Collection<Object>) oldValue;
			values.add(value);
			return;
		}
		throw new Exception("Cannot handle parameter " + name + ":" + value);
	}

	protected void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
		// parse cookies from headers first
		saveCookies();
		// other headers also
		saveHeaders();
		// if is GET method, parse the parameters from url
		if (request.method().equals(HttpMethod.GET)) {
			QueryStringDecoder queryDecoder = new QueryStringDecoder(msg.uri());
			parameters.putAll(queryDecoder.parameters());
			onGetRequest(ctx, msg);
		} else if (request.method().equals(HttpMethod.POST)) {
			decoder = new HttpPostRequestDecoder(factory, request);
			fileDatas = new ArrayList<FileUpload>();
			onPostRequestStart(ctx, msg);
		}
		
	}

	private void onPostRequestStart(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
		// TODO Auto-generated method stub
		
	}

	private void onGetRequest(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
		// TODO Auto-generated method stub
		
	}

	protected boolean isMyRequest(ChannelHandlerContext ctx, HttpObject msg) {
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;
		}
		if (request == null) {
			return false;
		}
		String uri = request.uri();
		if (!uri.startsWith(getSelfUriPrefix())) {
			return false;
		}
		if (!isValidMethod(request.method())) {
			return false;
		}
		return false;
	}

	protected boolean isValidMethod(HttpMethod method) {
		return true;
	}

	protected Logger logger() {
		if (myLogger == null) {
			myLogger = Logger.getLogger(this.getClass().getCanonicalName());
		}
		return myLogger;
	}

	protected void saveCookies() {
		String value = request.headers().get(HttpHeaderNames.COOKIE);
		if (value == null) {
			cookies = Collections.emptySet();
		} else {
			cookies = ServerCookieDecoder.STRICT.decode(value);
		}
	}

	protected void saveHeaders() {
		HttpHeaders reqheaders = request.headers();
		if (reqheaders == null) {
			return;
		}
		Iterator<Entry<String, String>> it = reqheaders.iteratorAsString();
		if (it == null) {
			return;
		}
		headers = new HashMap<String, String>();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			headers.put(entry.getKey(), entry.getValue());
		}
	}

	protected void reset() {
		request = null;
		if (decoder != null) {
			decoder.cleanFiles();
		}
		if (fileDatas != null) {
			for (FileUpload data : fileDatas) {
				data.release();
			}
			fileDatas.clear();
			fileDatas = null;
		}
	}

}
