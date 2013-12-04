package com.luxsoft.sw2.replica.producers;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw2.replica.Replicador;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

/**
 * Replicador basado en mensajes JMS sirve de base para mandar mensajes de replica desde la sucursal a las oficinas
 * o bien desde las oficinas a la sucursal
 * 
 * @author Ruben Cancino
 *
 */
public class JMSReplicator implements Replicador{
	
	Logger logger=LoggerHelper.getLogger();
	
	private String sucursal;
	
	private Destination destino;

	private JmsTemplate jmsTemplate;
	
	/**
	 * Manda ejecutar el mensaje de replica para el bean pero en una transaccion de Hibernate para 
	 * evitar una exception de tipo {@link org.hibernate.LazyInitializationException}
	 * 
	 */
	public void replicar(final Object bean) {
		try {
			final Object entity=resolver(bean);
			final Serializable id=(Serializable)PropertyUtils.getProperty(entity, "id");
			final Class clazz=entity.getClass();
			ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,SQLException {					
					Object entity=session.load(clazz, id);
					entity=((HibernateProxy)entity).getHibernateLazyInitializer().getImplementation();
					if(Abono.class.isAssignableFrom(entity.getClass()) ){
						Abono abono=(Abono)entity;
						Hibernate.initialize(abono.getAplicaciones());
					}
					EntityLog entityLog=new EntityLog(entity,id,sucursal,EntityLog.Tipo.CAMBIO);
					doReplicar(entityLog, jmsTemplate);		
					return null;
				}
			});
			
		} catch (Exception e) {
			String message=MessageFormat.format("Error enviando entidad {0} al topico: {1}  causa: {2}"
					,bean,destino,ExceptionUtils.getRootCauseMessage(e));
			logger.error(message);
			throw new RuntimeException(message);
		}		
	}
	
	/**
	 * Template method para re definir el bean a replicar, util para los paneles que utilizan un Transpor bean
	 * como ClienteRow2
	 * 
	 * @param bean
	 * @return
	 */
	protected Object resolver(final Object bean){
		return bean;
	}
	
	/**
	 * Genera el mensaje de replica
	 * 
	 * @param entity
	 * @param jmsTemplate
	 */
	protected void doReplicar(EntityLog entity,final JmsTemplate jmsTemplate){		
				jmsTemplate.convertAndSend(destino, entity,new MessagePostProcessor() {				
			public Message postProcessMessage(Message message) throws JMSException {
				message.setStringProperty("SUCURSA_ORIGEN", sucursal);
				return message;
			}
		});
		logger.info(" JMS enviado con entidad: "+entity+ " Destino: "+destino);		
	}

	
	@Required
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	@Required
	public void setDestino(Destination destino) {
		this.destino = destino;
	}
	
	@Required
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

}
