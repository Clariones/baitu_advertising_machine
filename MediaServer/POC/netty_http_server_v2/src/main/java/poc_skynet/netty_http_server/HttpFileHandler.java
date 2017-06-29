package poc_skynet.netty_http_server;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class HttpFileHandler extends SimpleChannelInboundHandler<HttpObject> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		System.out.println("Got " + msg.getClass().getCanonicalName());
		
		if (msg instanceof FullHttpRequest){
			dumpFullRequest((FullHttpRequest)msg);
		}else if (msg instanceof HttpRequest){
			dumpRequest((HttpRequest)msg);
		}else if (msg instanceof HttpContent){
			dumpContent((HttpContent)msg);
		}else{
			// 
		}
//		FullHttpResponse response = new DefaultFullHttpResponse(
//                HTTP_1_1, HttpResponseStatus.OK);
//        response.headers().set(CONTENT_TYPE, "text/html; encoding=utf-8");
//        response.headers().add("Access-Control-Allow-Origin", "*");
//
//        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void dumpFullRequest(FullHttpRequest msg) {
		System.out.println("dumpFullRequest()");
		HttpMethod method = msg.getMethod();
		String uri = msg.getUri();
		String content = msg.getDecoderResult().toString();
		System.out.println(method+": " + uri+"===>\n"+content);
		
		if (msg.getMethod().equals(HttpMethod.POST)){
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), msg);
			List<InterfaceHttpData> params = decoder.getBodyHttpDatas();
			for(InterfaceHttpData param : params){
				System.out.println("    >>" + param.getClass().getCanonicalName());
				String name = param.getName();
				HttpDataType dataType = param.getHttpDataType();
				switch (dataType){
				case Attribute:
					Attribute attribute = (Attribute) param;
					System.out.println("    Attribute " + name+"="+attribute.toString());
					break;
				case InternalAttribute:
					System.out.println("    InternalAttribute " + name+"="+param);
					break;
				case FileUpload:
					System.out.println("    FileUpload " + name+"="+param);
//					FileUpload fileUpload = (FileUpload) param;
//					ByteBuf fileContent = fileUpload.content();
//					System.out.println(fileContent.getBytes);
					break;
				}
			}
		}else{
			System.out.println("    GET request later");
		}
	}

	private void dumpContent(HttpContent msg) {
		System.out.println("dumpContent()");
		String uri = "content";
		String content = msg.getDecoderResult().toString();
		DecoderResult ctnt = msg.getDecoderResult();
		System.out.println(uri+"===>"+content);
		
	}

	private void dumpRequest(HttpRequest msg) {
		System.out.println("dumpRequest()");
		String uri = msg.getUri();
		String content = msg.getDecoderResult().toString();
		System.out.println(uri+"===>"+content);
		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), msg);
		List<InterfaceHttpData> params = decoder.getBodyHttpDatas();
		for(InterfaceHttpData param : params){
			System.out.println("    >>" + param.getClass().getCanonicalName());
			String name = param.getName();
			HttpDataType dataType = param.getHttpDataType();
			switch (dataType){
			case Attribute:
				Attribute attribute = (Attribute) param;
				System.out.println("    Attribute " + name+"="+attribute.toString());
				break;
			case InternalAttribute:
				System.out.println("    InternalAttribute " + name+"="+param);
				break;
			case FileUpload:
				System.out.println("    FileUpload " + name+"="+param);
//				FileUpload fileUpload = (FileUpload) param;
//				ByteBuf fileContent = fileUpload.content();
//				System.out.println(fileContent.getBytes);
				break;
			}
		}
		
	}

	

}
