package com.luxsoft.siipap.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import com.luxsoft.siipap.service.aop.EntityModificationEvent;

/**
 * Convierte un {@link EntityModificationEvent} a un JMS Message 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EntityEventConverter implements MessageConverter {

	public Object fromMessage(Message msg) throws JMSException,MessageConversionException {
		if (!(msg instanceof ObjectMessage))
			throw new MessageConversionException("Message isn't a ObjectMessage");
		ObjectMessage message = (ObjectMessage) msg;
		EntityModificationEvent info = (EntityModificationEvent) message.getObject();
		return info;
	}

	public Message toMessage(Object object, Session session)throws JMSException, MessageConversionException {
		if (!(object instanceof EntityModificationEvent)) {
			throw new MessageConversionException("Object not an instance of EntityModificationInfo");
		}
		EntityModificationEvent info = (EntityModificationEvent) object;
		ObjectMessage message = session.createObjectMessage();
		message.setObject(info);
		return message;
	}

}
