package com.luxsoft.siipap.jms.rpc.test.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerTest {
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext(
				"swx-jms-rpc-context.xml"
				,ServerTest.class
				);
		
		
	}

}
