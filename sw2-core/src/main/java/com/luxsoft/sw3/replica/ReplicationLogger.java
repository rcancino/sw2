package com.luxsoft.sw3.replica;

import java.util.HashSet;
import java.util.Set;

import javax.jms.Destination;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.siipap.cxc.model.Abono;





/**
 * Bean para detectar por medio del sistema de eventos de Hibernate  altas,bajas y cambios y 
 * enviarlos al sistema de replicacion 
 * 
 * USADO PARA REPLICAR DESDE LAS SUCURSALES
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicationLogger implements PostInsertEventListener
										,PostUpdateEventListener
										,PostDeleteEventListener
										,ApplicationContextAware{
	
	protected Logger logger=Logger.getLogger(getClass());
	
	private ApplicationContext ctx;
	
	private boolean activo=false;
	
	private Destination destino=new ActiveMQQueue("REPLICA.QUEUE");
	
	
	
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
		if(!activo)
			return;
		
		boolean abono=Abono.class.isAssignableFrom(entityLog.getBean().getClass());
		if(!getClases().contains(Abono.class))
			abono=false;
		
		if(getClases().contains(entityLog.getBean().getClass()) || abono ){			
			try {							
				getJmsTemplate().convertAndSend(destino, entityLog);
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

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.ctx=applicationContext;
		
	}
	
	
	private Set<Class> clases=new HashSet<Class>();
	
	public Set<Class> getClases() {
		return clases;
	}

	public void setClases(Set<Class> clases) {
		this.clases = clases;
	}
	
	private String sucursalOrigen;


	public String getSucursalOrigen() {
		return sucursalOrigen;
	}

	public void setSucursalOrigen(String sucursalOrigen) {
		this.sucursalOrigen = sucursalOrigen;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	
	
	
	
}
