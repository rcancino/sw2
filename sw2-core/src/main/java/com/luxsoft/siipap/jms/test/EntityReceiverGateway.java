package com.luxsoft.siipap.jms.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.siipap.jms.rpc.test.EntityModificationInfo;

public class EntityReceiverGateway {
	
	public EntityModificationInfo receiveInfo(){
		/*MapMessage message=(MapMessage)jmsTemplate.receive();
		EntityModificationInfo info=new EntityModificationInfo();
		try {
			info.setEntityClassName(message.getString("entityClassName"));
			info.setTipo(message.getString("tipo"));
		} catch (JMSException e) {
			throw JmsUtils.convertJmsAccessException(e);
		}
		System.out.println("Kool message received: "+info);
		return info;*/
		EntityModificationInfo info=(EntityModificationInfo)jmsTemplate.receiveAndConvert();
		System.out.println("Kool message received: "+info);
		return info;
	
	}
	
	private JmsTemplate jmsTemplate;

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext("swx-jms-context.xml",JmsSpringTest.class);
		EntityReceiverGateway g=(EntityReceiverGateway)ctx.getBean("entityReceiverGateway");
		g.receiveInfo();
	}
	

}
