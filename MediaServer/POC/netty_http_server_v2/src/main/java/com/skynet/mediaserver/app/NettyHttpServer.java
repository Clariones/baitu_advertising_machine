package com.skynet.mediaserver.app;

import org.springframework.beans.factory.annotation.Autowired;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class NettyHttpServer implements MediaServer{

	private int serverPort;
	private LogLevel logLevel;
	private boolean SSL;
	
	public boolean isSSL() {
		return SSL;
	}


	public void setSSL(boolean sSL) {
		SSL = sSL;
	}


	public int getServerPort() {
		return serverPort;
	}


	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}


	public LogLevel getLogLevel() {
		return logLevel;
	}


	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}


	@Autowired
	protected HttpServerInitializer httpServerInitializer;


	public void startWork() {
		
        
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
        	// Configure SSL.
            final SslContext sslCtx;
            if (SSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(SslProvider.JDK).build();
            } else {
                sslCtx = null;
            }
            httpServerInitializer.setSslContext(sslCtx);
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(getLogLevel()))
             .childHandler(httpServerInitializer);

            Channel ch = b.bind(serverPort).sync().channel();
            System.out.println("Media server starting to listen on port " + serverPort);
            ch.closeFuture().sync();
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}
}
