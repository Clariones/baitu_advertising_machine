package com.skynet.mediaserver.app;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.skynet.mediaserver.NettyPipelineHanlderFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
	@Autowired
	protected NettyPipelineHanlderFactory handlerFactory;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().build();

//        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
//        pipeline.addLast(new HttpContentCompressor());
        pipeline.addLast(new CorsHandler(corsConfig));

        List<ChannelHandler> handlers = handlerFactory.prepareHandlers();
        for(ChannelHandler handler: handlers){
        	pipeline.addLast(handler);
        }
        
	}

	

}
