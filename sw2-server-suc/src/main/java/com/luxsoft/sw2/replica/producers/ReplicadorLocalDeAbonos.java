package com.luxsoft.sw2.replica.producers;

import org.hibernate.Hibernate;
import org.springframework.jms.core.JmsTemplate;



import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.sw3.replica.EntityLog;

public class ReplicadorLocalDeAbonos extends JMSReplicator{
	
	/**
	 * Inicializamos las partidas para su replica
	 * 
	 */
	@Override
	protected void doReplicar(EntityLog entity, JmsTemplate jmsTemplate) {
		Abono abono=(Abono)entity.getBean();		
		Hibernate.initialize(abono.getAplicaciones());
		super.doReplicar(entity, jmsTemplate);
		
	}

}
