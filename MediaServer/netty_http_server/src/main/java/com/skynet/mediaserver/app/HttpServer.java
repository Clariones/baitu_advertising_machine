package com.skynet.mediaserver.app;

import org.springframework.beans.factory.annotation.Autowired;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServer {

	private int port = 8080;
	
	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	@Autowired
	protected HttpServerInitializer httpServerInitializer;


	public void startWork() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(httpServerInitializer);

            Channel ch = b.bind(port).sync().channel();

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}

}
