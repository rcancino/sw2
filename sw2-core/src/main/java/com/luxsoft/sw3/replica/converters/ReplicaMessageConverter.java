package com.luxsoft.sw3.replica.converters;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.utils.LoggerHelper;
import com.thoughtworks.xstream.XStream;

import flexjson.JSONSerializer;

/**
 * Message converter adecuado para 
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicaMessageConverter implements MessageConverter{

	
	
	
	Logger logger=LoggerHelper.getLogger();
	
	
	public Message toMessage(Object object, Session session)throws JMSException, MessageConversionException {
		
		String json=serialize(object);
		TextMessage message=session.createTextMessage();
		message.setText(json);
		message.setBooleanProperty("JSON", true);
		return message;
		
	}

	public Object fromMessage(Message message) throws JMSException,MessageConversionException {
		return null;
	}
	
	
	public String serialize(Object bean){
		if(Abono.class.isAssignableFrom(bean.getClass()) ){
			logger.info("Convirtiendo abono a formato JSON Abono: "+bean);
			/*
			String json=new JSONSerializer()
			.include("sucursal.id","cliente.id","cliente.clave","aplicaciones.cargo.id")
			.exclude("sucursal.*","cliente.*","aplicaciones.*")
			.exclude("*.class")
			.serialize(bean);
			return json;
			*/
			XStream xstream=new XStream();
			return xstream.toXML(bean);
		}else{
			return "PENDIENTE DE SERIALIZAR: "+bean.getClass().getName();
		}
	}

}
