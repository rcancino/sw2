package com.luxsoft.siipap.jms.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * Test para probar la recepcion de mensajes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReceiverTest {
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext("swx-jms-context.xml",JmsSpringTest.class);
		
		
	}

}
