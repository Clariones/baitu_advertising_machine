package com.skynet.mediaserver.nettyhandler;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;

import com.skynet.mediaserver.utils.FSMediaUtils;

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
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, FSMediaUtils.calcContentTypeByName(tgtFile.getName()));
		response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
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
}
