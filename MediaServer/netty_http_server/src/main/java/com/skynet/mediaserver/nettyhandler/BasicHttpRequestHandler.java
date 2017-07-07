package com.skynet.mediaserver.nettyhandler;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public abstract class BasicHttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> {
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    
	protected static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	protected static MimetypesFileTypeMap sMimetypesMap;
	protected Set<Cookie> cookies;
	protected HttpPostRequestDecoder decoder;
	protected List<FileUpload> fileDatas;
	protected HashMap<String, String> headers;
	protected Logger myLogger;
	protected Map<String, Object> parameters;
	protected HttpRequest request;

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

	
	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
		return isMyRequest(null, (HttpObject) msg);
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
//		if (!isMyRequest(ctx, msg)) {
//			ctx.fireChannelRead(msg);
//			return;
//		}
		logger().log(Level.FINE, "{0} handle request {1}", new Object[] {this.getClass().getSimpleName(), request.uri()});
		ensureMembers();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof HttpContent) {
			handleHttpContent(ctx, (HttpContent) msg);
		}
	}

	protected void ensureMembers() {
		if (parameters == null) {
			parameters = new HashMap<String, Object>();
		}
		if (fileDatas == null) {
			fileDatas = new ArrayList<FileUpload>();
		}
		if (cookies == null){
			cookies = new HashSet<Cookie>();
		}
	}

	public Set<Cookie> getCookies() {
		return cookies;
	}

	public HttpPostRequestDecoder getDecoder() {
		return decoder;
	}

	public List<FileUpload> getFileDatas() {
		return fileDatas;
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public HttpRequest getRequest() {
		return request;
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

	protected boolean isMyRequest(ChannelHandlerContext ctx, HttpObject msg) {
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;
		}
		if (request == null) {
			return false;
		}
		String uri = request.uri();
		if (!isUriMatched(uri)) {
			return false;
		}
		if (!isValidMethod(request.method())) {
			return false;
		}
		return true;
	}

	protected boolean isUriMatched(String uri) {
		return uri.startsWith(getSelfUriPrefix());
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

	protected void onGetRequest(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
		onRequest(ctx);
	}

	protected void onHttpAttribute(Attribute attribute) throws Exception {
		addReqParam(attribute.getName(), attribute.getValue());
	}

	protected void onHttpFileUpload(FileUpload data) {
		fileDatas.add((FileUpload) data);
	}

	protected void onOtherHttpData(InterfaceHttpData data) throws Exception {
		throw new Exception("You must handle the " + data.getClass().getCanonicalName() + " yourself");
	}

	protected void onPostRequestRecieved(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
		onRequest(ctx);
	}

	protected void onPostRequestStart(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
		// by default, nothing to do
	}

	protected abstract void onRequest(ChannelHandlerContext ctx) throws Exception;

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

	protected void responseException(ChannelHandlerContext ctx, Exception e1) {
		// TODO Auto-generated method stub

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

	public void setCookies(Set<Cookie> cookies) {
		this.cookies = cookies;
	}

	public void setDecoder(HttpPostRequestDecoder decoder) {
		this.decoder = decoder;
	}

	public void setFileDatas(List<FileUpload> fileDatas) {
		this.fileDatas = fileDatas;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	protected void writeStringResponse(ChannelHandlerContext ctx, HttpResponseStatus httpStatus, String msg, String contentType, boolean sendCookieBack) {
		ByteBuf buf = copiedBuffer(msg, CharsetUtil.UTF_8);
		boolean close = request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, true)
	            || request.protocolVersion().equals(HttpVersion.HTTP_1_0)
	            && !request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true);
		FullHttpResponse response = new DefaultFullHttpResponse(
	            HttpVersion.HTTP_1_1, httpStatus, buf);
	    response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
	    if (!close) {
	        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
	    }
	    if (sendCookieBack && !getCookies().isEmpty()){
	    	for (Cookie cookie : getCookies()) {
	            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
	        }
	    }
	    ChannelFuture future = ctx.writeAndFlush(response);
	    if (close) {
	        future.addListener(ChannelFutureListener.CLOSE);
	    }
	    
	}

	protected void sendStaticFile(ChannelHandlerContext ctx, File tgtFile, HttpRequest request) throws Exception {

        File file = tgtFile;
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        if (!file.isFile()) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
                return;
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            sendFileFuture =
                    ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture =
                    ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                            ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            //@Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) { // total unknown
                    System.err.println(future.channel() + " Transfer progress: " + progress);
                } else {
                    System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
                }
            }

            //@Override
            public void operationComplete(ChannelProgressiveFuture future) {
                System.err.println(future.channel() + " Transfer complete.");
            }
        });

        // Decide whether to close the connection or not.
        if (!HttpUtil.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }
    protected static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * When file time stamp is the same as what the browser is sending up, send a "304 Not Modified"
     *
     * @param ctx
     *            Context
     */
    protected static void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response
     *            HTTP response
     */
    protected static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param fileToCache
     *            file to extract content type
     */
    protected static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "protected, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    protected static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = getMimeTypesMapInstance();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
    }
    
    private static MimetypesFileTypeMap getMimeTypesMapInstance() {
		if (sMimetypesMap == null){
			synchronized (BasicHttpRequestHandler.class){
				if (sMimetypesMap == null){
					sMimetypesMap = new MimetypesFileTypeMap();
				}
			}
		}
		return sMimetypesMap;
	}


	protected static void sendNotFound(ChannelHandlerContext ctx, String uri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND,
        		Unpooled.copiedBuffer("Not Found: " + uri + "\r\n", CharsetUtil.UTF_8));
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
