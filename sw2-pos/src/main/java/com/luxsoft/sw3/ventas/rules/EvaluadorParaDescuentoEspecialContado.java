package com.luxsoft.sw3.ventas.rules;

import org.apache.log4j.Logger;


import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.sw3.model.Evaluador;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Implementacion de {@link Evaluador} para determina si un pedido 
 * de contado requiere autorizacion por tratarse de descuento especial
 * 
 * @author Ruben Cancino
 *
 */
public class EvaluadorParaDescuentoEspecialContado implements Evaluador{
	
	private Logger logger=Logger.getLogger(getClass());

	
	public String requiereParaSalvar(Object bean) {
		Pedido pedido=(Pedido)bean;
		// Verificar si es de credito
		if(!pedido.isDeCredito()){
			// Verificar si no tiene ya una autorizacion asignada
			if(pedido.getAutorizacion()==null){
				if(pedido.getDescuento()!=pedido.getDescuentoOrigen()){
					if(logger.isDebugEnabled()){
						logger.debug("El pedido tiene un descuento especial");
					}
					return "DESCUENTO ESPECIAL DE CONTADO";
				}
			}
		}/*else if(pedido.isDeCredito()){
			ClienteCredito cr=pedido.getCliente().getCredito();
			if(cr.getCreditoDisponible().compareTo(pedido.getTotal())<0)
				return "LINEA DE CREDITO EXCEDIDA";
		}	*/
		else if(pedido.isDeCredito() && (pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))){
			// Verificar si no tiene ya una autorizacion asignada
			if(pedido.getAutorizacion()==null){
				if(pedido.getDescuento()!=pedido.getDescuentoOrigen()){
					if(logger.isDebugEnabled()){
						logger.debug("El pedido tiene un descuento especial");
					}
					return "DESCUENTO ESPECIAL DE CREDITO";
				}
			}
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
