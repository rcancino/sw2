package com.luxsoft.siipap.jms.rpc.test.lingo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestServiceServer {
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext("swx-jms-rpc-context.xml",TestServiceServer.class);
		
		
	}

}
