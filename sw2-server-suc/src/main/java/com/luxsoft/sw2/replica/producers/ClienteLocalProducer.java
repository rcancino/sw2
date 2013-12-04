package com.luxsoft.sw2.replica.producers;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteRow2;

/**
 * Sub clase de JMSReplicator para afinar la replica de registros de cliente
 * 
 * @Todo Mover logica a MessageConverter
 * @author Ruben Cancino
 *
 */
public class ClienteLocalProducer extends JMSReplicator{
	
	@Override
	protected Object resolver(Object bean) {
		ClienteRow2 row=(ClienteRow2)bean;
		Cliente c=new Cliente();
		c.setId(row.getCliente_id());
		c.setClave(row.getClave());
		return c;
		
	}

}
