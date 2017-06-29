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
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class CommonFileGetHandler extends SimpleChannelInboundHandler<HttpObject> {

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

    private void writeFile(ChannelHandlerContext ctx, String path) {
    	String fileName = path.substring(5);
    	if (fileName.isEmpty() || fileName.equalsIgnoreCase("/")){
    		fileName="index.html";
    	}
    	File tgtFile = new File(getBaseFolderPath(), fileName);
    	System.out.println("processing " + tgtFile.getAbsolutePath());
    	FullHttpResponse response;
    	
    	if (tgtFile.exists()) {
	    	ByteBuf buf = wrappedBuffer(readFileAsBytes(tgtFile));
	        // Build the response object.
	        response = new DefaultFullHttpResponse(
	                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
	
	        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
    	}else{
    		ByteBuf buf = copiedBuffer(fileName+" not found", CharsetUtil.UTF_8);;
	        // Build the response object.
	        response = new DefaultFullHttpResponse(
	                HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, buf);
	
	        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	        response.headers().set(CONTENT_LENGTH, buf.readableBytes());
    	}

        // Write the response.
        ctx.channel().writeAndFlush(response);
	}

	private File getBaseFolderPath() {
		return new File(baseFolder);
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
