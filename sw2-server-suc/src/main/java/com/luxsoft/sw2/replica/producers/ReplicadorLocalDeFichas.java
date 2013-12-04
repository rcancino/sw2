package com.luxsoft.sw2.replica.producers;

import org.hibernate.Hibernate;
import org.springframework.jms.core.JmsTemplate;


import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.sw3.replica.EntityLog;

public class ReplicadorLocalDeFichas extends JMSReplicator{
	
	/**
	 * Impedimos que se manden los pedidos
	 * 
	 */
	@Override
	protected void doReplicar(EntityLog entity, JmsTemplate jmsTemplate) {
		Ficha ficha=(Ficha)entity.getBean();		
		Hibernate.initialize(ficha.getPartidas());
		super.doReplicar(entity, jmsTemplate);
	}

}
