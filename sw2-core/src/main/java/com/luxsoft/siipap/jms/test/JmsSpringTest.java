package com.luxsoft.siipap.jms.test;

import java.util.Date;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.jms.rpc.test.EntityModificationGateway;
import com.luxsoft.siipap.jms.rpc.test.EntityModificationInfo;
import com.luxsoft.siipap.jms.rpc.test.EntityModificationInfo.Tipo;

public class JmsSpringTest implements EntityModificationGateway{
	
	
	
	public void sendInfo(final EntityModificationInfo info) {
		jmsTemplate.convertAndSend(info);
		/*jmsTemplate.send(new MessageCreator(){
			public Message createMessage(Session session) throws JMSException {
				MapMessage message=session.createMapMessage();
				message.setString("entityClassName", info.getEntityClassName());
				message.setString("tipo", EntityModificationInfo.Tipo.INSERT.name());
				
				return message;
			}			
		});*/
		
	}
	
	private JmsTemplate jmsTemplate;
	
	

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	
	
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext("swx-jms-context.xml",JmsSpringTest.class);
		EntityModificationGateway g=(EntityModificationGateway)ctx.getBean("entityModificationGateway");
		EntityModificationInfo info=new EntityModificationInfo(Cargo.class,"TEST");
		info.setTime(new Date());
		info.setTipo(Tipo.INSERT);
		g.sendInfo(info);
	}

}
