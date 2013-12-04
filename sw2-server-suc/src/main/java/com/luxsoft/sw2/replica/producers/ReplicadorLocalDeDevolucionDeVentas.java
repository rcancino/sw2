package com.luxsoft.sw2.replica.producers;

import org.hibernate.Hibernate;
import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.sw3.replica.EntityLog;

public class ReplicadorLocalDeDevolucionDeVentas extends JMSReplicator{
	
	/**
	 * Impedimos que se manden los pedidos
	 * 
	 */
	@Override
	protected void doReplicar(EntityLog entity, JmsTemplate jmsTemplate) {
		Devolucion devo=(Devolucion)entity.getBean();
		Hibernate.initialize(devo.getPartidas());
		super.doReplicar(entity, jmsTemplate);
	}

}
