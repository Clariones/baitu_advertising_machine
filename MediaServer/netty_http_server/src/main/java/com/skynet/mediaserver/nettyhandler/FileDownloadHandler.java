package com.skynet.mediaserver.nettyhandler;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.DATE;
import static io.netty.handler.codec.http.HttpHeaders.Names.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class FileDownloadHandler extends SimpleChannelInboundHandler<HttpObject> {

	protected String pathPrefix = "/public";
	private String baseFolder;
	
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
		
		writeFile(ctx, uri);

	}

    private void writeFile(ChannelHandlerContext ctx, String path) throws Exception {
    	int pos = path.indexOf('?');
    	if (pos > 0){
    		path = path.substring(0,  pos);
    	}
    	String fileName = URLDecoder.decode(path, "UTF-8").substring(pathPrefix.length());
  
    	if (fileName.isEmpty() || fileName.equalsIgnoreCase("/")){
    		fileName="index.html";
    	}
    	File tgtFile = new File(getBaseFolderPath(), fileName);
    	System.out.println("processing " + tgtFile.getAbsolutePath());
    	FullHttpResponse response;
    	
    	if (tgtFile.exists()) {
    		sendStaticFile(ctx, tgtFile);
//	        // Build the response object.
//	        response = new DefaultFullHttpResponse(
//	                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
//	        
//	        response.headers().set(CONTENT_TYPE, FSMediaUtils.calcContentTypeByName(fileName));
//	        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
    		return;
    	}else{
    		ByteBuf buf = copiedBuffer(fileName+" not found", CharsetUtil.UTF_8);;
	        // Build the response object.
	        response = new DefaultFullHttpResponse(
	                HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, buf);
	
	        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
	        ctx.channel().writeAndFlush(response);
    	}
	}

	private File getBaseFolderPath() {
		return new File(baseFolder);
	}

	private void sendStaticFile(ChannelHandlerContext ctx, File tgtFile) throws Exception {
		// Write the content.
		ByteBuf buf = copiedBuffer(readFileAsBytes(tgtFile));
		// Build the response object.
		HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);

		setContentTypeHeader(response, tgtFile);
		setDateAndCacheHeaders(response, tgtFile);
		// response.headers().set(HttpHeaderNames.CONTENT_TYPE,
		// FSMediaUtils.calcContentTypeByName(tgtFile.getName()));
		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
		response.headers().set(HttpHeaderNames.ACCEPT_RANGES, "bytes");
		ctx.channel().writeAndFlush(response);
//		ctx.channel().write(response);
	}
	private void sendStaticFile1(ChannelHandlerContext ctx, File tgtFile) throws Exception {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		setContentTypeHeader(response, tgtFile);
		setDateAndCacheHeaders(response, tgtFile);
		//response.headers().set(HttpHeaderNames.CONTENT_TYPE, FSMediaUtils.calcContentTypeByName(tgtFile.getName()));
		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, tgtFile.length());
//		response.headers().set(HttpHeaderNames.ACCEPT_RANGES, "bytes");
		ctx.channel().write(response);
		// Write the content.
		long fileLength = tgtFile.length();
		RandomAccessFile raf = new RandomAccessFile(tgtFile, "r");
		ctx.channel().writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
				ctx.newProgressivePromise());
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

		lastContentFuture.addListener(ChannelFutureListener.CLOSE);

	}

	private byte[] readFileAsBytes(File tgtFile) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			FileInputStream fin = new FileInputStream(tgtFile);
			byte[] buf = new byte[4*1024];
			int n = 0;
			while((n=fin.read(buf)) > 0){
				bout.write(buf, 0, n);
			}
			fin.close();
			return bout.toByteArray();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	     public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	     public static final int HTTP_CACHE_SECONDS = 60;
	private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
		response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
		response.headers().set(LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
	}
}
