package com.luxsoft.sw3.replica;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import com.luxsoft.utils.LoggerHelper;





/**
 * Implmenetacion de Listeners de hibernate para generar archivos de replica para cada operacion
 * de persistencia
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicaListener implements PostInsertEventListener
										,PostUpdateEventListener
										,PostDeleteEventListener
										,ApplicationContextAware
										,InitializingBean {
	
	protected Logger logger=LoggerHelper.getLogger();
	
	private ApplicationContext ctx;
	
	private boolean replicar=false;
	
	
	public void onPostInsert(PostInsertEvent event) {
		replicar(new EntityLog(event,getSucursalOrigen()));
	}	

	public void onPostUpdate(PostUpdateEvent event) {
		replicar(new EntityLog(event,getSucursalOrigen()));	
	}

	public void onPostDelete(PostDeleteEvent event) {
		replicar(new EntityLog(event,getSucursalOrigen()));
	}
	
	private void replicar(EntityLog entityLog){
		
		if(entityLog.getBean() instanceof Replicable ){			
			try {
				String destino=entityLog.getDestinationOut();
				getJmsTemplate().convertAndSend(destino, entityLog,new MessagePostProcessor() {
					public Message postProcessMessage(Message message) throws JMSException {
						//message.setJMSPriority(priority)
						message.setStringProperty("SUCURSAL_ORIGEN", getSucursalOrigen());
						return message;
					}
				});
				logger.info("Entidad enviada al JMS Broker. Desc: "+entityLog);
			} catch (Exception e) {
				logger.error("Error enviando mensaje de replica...",e);
			}			
		}
	}
	
	private JmsTemplate jmsTemplate;
	
	public JmsTemplate getJmsTemplate() {
		if(this.jmsTemplate==null){
			this.jmsTemplate=(JmsTemplate)ctx.getBean("jmsTemplate");			
		}
		return jmsTemplate;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.ctx=applicationContext;
	}

	public void afterPropertiesSet() throws Exception {		
		logger.info("Replicador registrado para : "+getSucursalOrigen());
	}
	
	private String sucursalOrigen;


	public String getSucursalOrigen() {
		return sucursalOrigen;
	}

	public void setSucursalOrigen(String sucursalOrigen) {
		this.sucursalOrigen = sucursalOrigen;
	}

	public boolean isReplicar() {
		return replicar;
	}

	public void setReplicar(boolean replicar) {
		this.replicar = replicar;
	}
	
	
	
}
