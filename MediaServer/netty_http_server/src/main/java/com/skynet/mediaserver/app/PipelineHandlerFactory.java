package com.skynet.mediaserver.app;

import io.netty.channel.ChannelHandler;

public interface PipelineHandlerFactory {

	ChannelHandler createHandler();

}
