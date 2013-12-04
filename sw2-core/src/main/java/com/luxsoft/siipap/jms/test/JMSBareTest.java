package com.luxsoft.siipap.jms.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

/**
 * Probamos una transmision de JMS simple sin ayuda de Spring
 * conectandonos a un broker local
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class JMSBareTest {
	
	
	public static void sendMessage(){
		ConnectionFactory cf=new ActiveMQConnectionFactory("tcp://localhost:61616");
		Connection con=null;
		Session session=null;
		
		try {
			con=cf.createConnection();
			session=con.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination=new ActiveMQQueue("autorizacionDeAbonos");
			
			MessageProducer producer=session.createProducer(destination);
			TextMessage message=session.createTextMessage();
			message.setText("Hello World 2 of JMS");
			producer.send(message);
		} catch (JMSException e) {			
			e.printStackTrace();
		} finally{			
				try {
					if(session!=null)	session.close();
					if(con!=null) 		con.close();
				} catch (JMSException e) {					
					e.printStackTrace();
				}
		}
	}
	
	public static void receiveMessage(){
		ConnectionFactory cf=new ActiveMQConnectionFactory("tcp://localhost:61616");
		Connection con=null;
		Session session=null;
		
		try {
			con=cf.createConnection();
			session=con.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination=new ActiveMQQueue("autorizacionDeAbonos");
			
			MessageConsumer consumer=session.createConsumer(destination);
			con.start();
			
			Message message=consumer.receive();
			
			TextMessage tm=(TextMessage)message;
			System.out.println("Kool I'v got a message:"+tm.getText());
			
		} catch (JMSException e) {			
			e.printStackTrace();
		} finally{			
				try {
					if(session!=null)	session.close();
					if(con!=null) 		con.close();
				} catch (JMSException e) {					
					e.printStackTrace();
				}
		}
	}
	
	public static void main(String[] args) {		
		receiveMessage();
	}

}
