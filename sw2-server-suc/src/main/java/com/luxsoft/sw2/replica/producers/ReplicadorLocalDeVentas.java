package com.luxsoft.sw2.replica.producers;

import org.hibernate.Hibernate;
import org.springframework.jms.core.JmsTemplate;

import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.replica.EntityLog;

public class ReplicadorLocalDeVentas extends JMSReplicator{
	
	/**
	 * Impedimos que se manden los pedidos
	 * 
	 */
	@Override
	protected void doReplicar(EntityLog entity, JmsTemplate jmsTemplate) {
		Venta venta=(Venta)entity.getBean();
		venta.setPedido(null);
		Hibernate.initialize(venta.getPartidas());
		super.doReplicar(entity, jmsTemplate);
	}

}
