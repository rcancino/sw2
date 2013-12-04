package com.luxsoft.siipap.cxc.rules;

import org.springframework.util.Assert;

import com.luxsoft.siipap.model.core.Cliente;

/**
 * Reglas de negocios asociadas con la aplicacion de pagos 
 * 
 * @author Ruben Cacino
 *
 */
public class AbstractAplicacionDePagoRules {
	
	/**
	 * En esta implementacion no se ha definido ningun comportamiento en especifico
	 * 
	 * @param cliente
	 */
	public void validarCliente(final Cliente cliente){
		Assert.isTrue(!cliente.isSuspendido(),"El cliente esta suspendido para todo tipo de operación");
	}

}
