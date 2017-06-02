package com.skynet.mediaserver.nettyhandler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.skynet.mediaserver.NettyPipelineHanlderFactory;

import io.netty.channel.ChannelHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class DefaultHandlerFactory implements NettyPipelineHanlderFactory,ApplicationContextAware {
	private ApplicationContext applicationContext;


	public List<ChannelHandler> prepareHandlers() {
		List<ChannelHandler> results = new ArrayList<ChannelHandler>();
		results.add((ChannelHandler) applicationContext.getBean("fileUploadHandler"));
		results.add((ChannelHandler) applicationContext.getBean("commonFileGetHandler"));
		results.add((ChannelHandler) applicationContext.getBean("fileSearHandler"));
		results.add(new ChunkedWriteHandler());
		results.add((ChannelHandler) applicationContext.getBean("fileDownloadHandler"));
		
		return results;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
