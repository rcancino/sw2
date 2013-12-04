package com.luxsoft.sw2.replica.producers;

import org.hibernate.Hibernate;
import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.sw3.replica.EntityLog;

public class ReplicadorLocalDeRecepcionesDeCompras extends JMSReplicator{
	
	/**
	 * Impedimos que se repliquen compras depuradas o totalmente atendidas
	 * 
	 */
	@Override
	protected void doReplicar(EntityLog entity, JmsTemplate jmsTemplate) {
		RecepcionDeCompra r=(RecepcionDeCompra)entity.getBean();		
		Hibernate.initialize(r.getPartidas());
		super.doReplicar(entity, jmsTemplate);
	}

}
