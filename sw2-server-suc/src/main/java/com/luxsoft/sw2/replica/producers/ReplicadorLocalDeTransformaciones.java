package com.luxsoft.sw2.replica.producers;

import org.hibernate.Hibernate;
import org.springframework.jms.core.JmsTemplate;


import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.sw3.replica.EntityLog;

public class ReplicadorLocalDeTransformaciones extends JMSReplicator{
	
	/**
	 * Inicializacion de las partidas de las transformaciones
	 * 
	 */
	@Override
	protected void doReplicar(EntityLog entity, JmsTemplate jmsTemplate) {
		Transformacion trs=(Transformacion)entity.getBean();		
		Hibernate.initialize(trs.getPartidas());
		super.doReplicar(entity, jmsTemplate);
	}

}
