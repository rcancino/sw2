package com.luxsoft.sw3.replica.converters;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class AbonoConverter implements MessageConverter{
	
	private JSONSerializer ser;
	private JSONDeserializer des;
	Logger logger=LoggerHelper.getLogger();	
	
	public AbonoConverter(){
		ser=new JSONSerializer();		
		des=new JSONDeserializer();		
		initSerializer();
	}
	
	protected void initSerializer(){
		ser.exclude("*.class")
		
		;
	}
	
	protected void addMessageProperties(TextMessage message,EntityLog log) throws JMSException{
		
	}
	
	public Message toMessage(Object object, Session session)throws JMSException, MessageConversionException {
		EntityLog log=(EntityLog)object;
		Object entity=log.getBean();
		String json=ser.serialize(entity);
		TextMessage message=session.createTextMessage();
		message.setText(json);
		message.setBooleanProperty("JSON", true);
		message.setStringProperty("SUCURSAL_ORIGEN", log.getSucursalOrigen());
		message.setStringProperty("TIPO", log.getTipo().name());
		addMessageProperties(message, log);
		return message;
	}
	
	public Object fromMessage(Message message) throws JMSException,MessageConversionException {
		
		return null;
	}
	
	
}
