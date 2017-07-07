package com.skynet.mediaserver.app;

import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslContext;

public interface HttpServerInitializer extends ChannelHandler{

	void setSslContext(SslContext sslCtx);

}
