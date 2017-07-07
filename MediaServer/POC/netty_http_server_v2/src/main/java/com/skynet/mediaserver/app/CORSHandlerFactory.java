package com.skynet.mediaserver.app;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;

public class CORSHandlerFactory implements PipelineHandlerFactory {

	public ChannelHandler createHandler() {
		CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().build();
		return new CorsHandler(corsConfig);
	}

}
