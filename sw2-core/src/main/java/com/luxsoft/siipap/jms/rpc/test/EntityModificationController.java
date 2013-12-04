package com.luxsoft.siipap.jms.rpc.test;

import java.util.HashSet;
import java.util.Set;

import org.springframework.jms.core.JmsTemplate;



/**
 * 
 * Genera un JMS Message a por cada modificacion en una entidad
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EntityModificationController {
	
	private Set<Class> clases=new HashSet<Class>();
	
	public void entityModification(EntityModificationInfo info){
		if(clases.contains(info.getEntityClass()))
			jmsTemplate.convertAndSend(info);
	}
	
	private JmsTemplate jmsTemplate;
	

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}


	public Set<Class> getClases() {
		return clases;
	}

	public void setClases(Set<Class> clases) {
		this.clases = clases;
	}
	
	

}
