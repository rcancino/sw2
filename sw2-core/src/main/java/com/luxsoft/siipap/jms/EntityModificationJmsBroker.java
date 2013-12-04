package com.luxsoft.siipap.jms;

import java.util.HashSet;
import java.util.Set;

import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.siipap.service.aop.EntityModificationEvent;
import com.luxsoft.siipap.service.aop.EntityModificationListener;

/**
 * Manda un mensaje JMS por cada modificacion  de la clase que 
 * este intersado
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class EntityModificationJmsBroker implements EntityModificationListener{
	
	private Set<String> clases=new HashSet<String>();

	public void onEntityModification(EntityModificationEvent event) {
		//if(clases.contains(event.getEntityClassName()))
			//jmsTemplate.convertAndSend(event);
	}
	
	private JmsTemplate jmsTemplate;
	

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}


	public Set<String> getClases() {
		return clases;
	}

	public void setClases(Set<String> clases) {
		this.clases = clases;
	}

}
