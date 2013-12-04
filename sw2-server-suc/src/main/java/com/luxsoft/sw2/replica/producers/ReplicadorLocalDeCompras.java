package com.luxsoft.sw2.replica.producers;

import org.hibernate.Hibernate;
import org.springframework.jms.core.JmsTemplate;


import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.sw3.replica.EntityLog;

public class ReplicadorLocalDeCompras extends JMSReplicator{
	
	/**
	 * Inicializamos las partidas para su replica
	 * 
	 */
	@Override
	protected void doReplicar(EntityLog entity, JmsTemplate jmsTemplate) {
		Compra2 compra=(Compra2)entity.getBean();		
		Hibernate.initialize(compra.getPartidas());
		super.doReplicar(entity, jmsTemplate);
		
	}

}
