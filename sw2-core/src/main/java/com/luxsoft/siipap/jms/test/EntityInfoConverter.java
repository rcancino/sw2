package com.luxsoft.siipap.jms.test;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.luxsoft.siipap.jms.rpc.test.EntityModificationInfo;

public class EntityInfoConverter implements MessageConverter{

	/*public Object fromMessage(Message msg) throws JMSException,MessageConversionException {
		if(!(msg instanceof MapMessage))
			throw new MessageConversionException("Message isn't a MapMessage");
		MapMessage message=(MapMessage)msg;
		EntityModificationInfo info=new EntityModificationInfo();
		info.setEntityClassName(message.getString("entityClassName"));
		info.setTipo(message.getString("tipo"));
		return info;
	}

	public Message toMessage(Object object, Session session) throws JMSException,MessageConversionException {
		if(!(object instanceof EntityModificationInfo)){
			throw new MessageConversionException("Object not an instance of EntityModificationInfo");
		}
		EntityModificationInfo info=(EntityModificationInfo)object;
		MapMessage message=session.createMapMessage();
		message.setString("entityClassName", info.getEntityClassName());
		message.setString("tipo", EntityModificationInfo.Tipo.INSERT.name());
		
		return message;
	}
*/
	
	/**
	 * 
	 */
	public Object fromMessage(Message msg) throws JMSException,
			MessageConversionException {
		if (!(msg instanceof ObjectMessage))
			throw new MessageConversionException("Message isn't a ObjectMessage");
		ObjectMessage message = (ObjectMessage) msg;
		EntityModificationInfo info =(EntityModificationInfo)message.getObject();
		
		//info.setEntityClassName(message.getString("entityClassName"));
		//info.setTipo(message.getString("tipo"));
		return info;
	}

	public Message toMessage(Object object, Session session)
			throws JMSException, MessageConversionException {
		if (!(object instanceof EntityModificationInfo)) {
			throw new MessageConversionException(
					"Object not an instance of EntityModificationInfo");
		}
		EntityModificationInfo info = (EntityModificationInfo) object;
		ObjectMessage message = session.createObjectMessage();
		message.setObject(info);		
		return message;
	}

}
