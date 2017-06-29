package com.skynet.mediaserver.app;

import java.util.List;

import com.skynet.mediaserver.PipelineHandlerFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

public class BasicHttpServerInitializer extends ChannelInitializer<SocketChannel> implements HttpServerInitializer {
	protected SslContext sslContext;

	public SslContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SslContext sslContext) {
		this.sslContext = sslContext;
	}

	private List<PipelineHandlerFactory> handlerFactories;

	public List<PipelineHandlerFactory> getHandlerFactories() {
		return handlerFactories;
	}

	public void setHandlerFactories(List<PipelineHandlerFactory> handlerFactories) {
		this.handlerFactories = handlerFactories;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (getSslContext() != null) {
			pipeline.addLast(getSslContext().newHandler(ch.alloc()));
		}
		for (PipelineHandlerFactory factory : handlerFactories) {
			pipeline.addLast(factory.createHandler());
		}
	}
}
