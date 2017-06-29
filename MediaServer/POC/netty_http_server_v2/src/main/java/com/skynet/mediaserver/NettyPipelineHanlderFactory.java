package com.skynet.mediaserver;

import java.util.List;

import io.netty.channel.ChannelHandler;

public interface NettyPipelineHanlderFactory {

	List<ChannelHandler> prepareHandlers();

}
