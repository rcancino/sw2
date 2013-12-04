package com.luxsoft.sw3.aop;

import java.math.BigDecimal;

import com.luxsoft.siipap.cxc.service.ClienteServices;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;

public class ActualizarSaldoAtrasoClienteTask implements Runnable{
	
	private final Cliente cliente;
	
	public ActualizarSaldoAtrasoClienteTask(Cliente cliente){
		this.cliente=cliente;
	}
	
	public void run() {
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(cliente.getClave());
		ClienteServices s=ServiceLocator2.getClienteServices();
		Object[] datos=s.calcularSaldoyAtraso(cliente);
		c.getCredito().setSaldo((BigDecimal)datos[0]);
		c.getCredito().setAtrasoMaximo((Integer)datos[0]);
		ServiceLocator2.getClienteManager().save(c);
		
	}
	
}
