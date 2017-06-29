package com.skynet.mediaserver;

import io.netty.channel.ChannelHandler;

public interface PipelineHandlerFactory {

	ChannelHandler createHandler();

}
