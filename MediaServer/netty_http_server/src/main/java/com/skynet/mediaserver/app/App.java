package com.skynet.mediaserver.app;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Map<String, String> envs = System.getenv();
    	for(Entry<String, String> entry : envs.entrySet()){
    		System.out.println(entry.getKey()+"="+ entry.getValue());
    	}
        System.out.println( "Starting POC http server....." );
        
        ApplicationContext context = new FileSystemXmlApplicationContext("conf/spring.main.xml");
        
        HttpServer server = (HttpServer) context.getBean("nettyServer");
        server.startWork();
    }
}
