package com.skynet.mediaserver.app;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.skynet.mediaserver.PipelineHandlerFactory;

import io.netty.channel.ChannelHandler;

public class BasicPipelineHandlerFactory implements PipelineHandlerFactory,ApplicationContextAware {
	private ApplicationContext applicationContext;
	private String handlerName;
	
	public String getHandlerName() {
		return handlerName;
	}
	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}
	public ChannelHandler createHandler() {
		return (ChannelHandler) applicationContext.getBean(getHandlerName());
	}
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
}
