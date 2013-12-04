package com.luxsoft.sw3.ventas.rules;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.sw3.model.Evaluador;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Implementacion de {@link Evaluador} requiere autorizacion del departamento
 * de CXC por tratarse de clientes con linea de credito saturada
 * o bien tener credito suspendido.
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class EvaluadorDeLineaDeCredito implements Evaluador{
	
	private Logger logger=Logger.getLogger(getClass());

	
	public String requiereParaSalvar(Object bean) {
		Pedido pedido=(Pedido)bean;
		// Verificar si es de credito
		if((pedido!=null) && pedido.isDeCredito()){
			ClienteCredito cr=pedido.getCliente().getCredito();
			if(cr.getCreditoDisponible().compareTo(pedido.getTotal())<0)
				return "LINEA DE CREDITO EXCEDIDA";
		}		
		return null;
	}

	
	public String requiereParaEliminar(Object entidad) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String requiereParaActualizar(Object entidad) {
		// TODO Auto-generated method stub
		return null;
	}

}
