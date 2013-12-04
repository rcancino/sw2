package com.luxsoft.sw2.replica.parches;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.ClassUtils;


import com.luxsoft.sw3.replica.EntityLog;

public class DLQConsumer {
	
	
	public static void buscarEnProduccion() throws Exception{
		
		ApplicationContext context=new ClassPathXmlApplicationContext(ClassUtils.addResourcePathToPackagePath(DLQConsumer.class, "dlq-connections.xml"));
		ActiveMQConnectionFactory connectionFactory=(ActiveMQConnectionFactory)context.getBean("cf5febreroConnectionFactory");
		JmsTemplate template=new JmsTemplate(connectionFactory);
		Message message=template.receive("ActiveMQ.DLQ");
		ObjectMessage om=(ObjectMessage)message;
		EntityLog log=(EntityLog)om.getObject();
		System.out.println(log);		
		System.out.print("\n\n\n"+ToStringBuilder.reflectionToString(log.getBean(),ToStringStyle.MULTI_LINE_STYLE));
		
	}
	
	
	public static void main(String[] args) throws Exception {
		buscarEnProduccion();
	}

}
