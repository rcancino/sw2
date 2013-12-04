package com.luxsoft.sw3.ventas.rules;

import org.apache.log4j.Logger;


import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.sw3.model.Evaluador;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;

/**
 * Implementacion de {@link Evaluador} para determina si un pedido 
 * de credito requiere autorizacion por tratarse de descuento especial
 * 
 * @author Ruben Cancino
 *
 */
public class EvaluadorParaPrecioEspecialCredito implements Evaluador{
	
	private Logger logger=Logger.getLogger(getClass());

	
	public String requiereParaSalvar(Object bean) {
		Pedido pedido=(Pedido)bean;
		// Verificar si es de credito
		if(pedido.isDeCredito() && (!pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))){
			// Verificar si no tiene ya una autorizacion asignada
			if(pedido.getAutorizacion()==null){
				for(PedidoDet det:pedido.getPartidas()){
					if(det.getProducto().getClave().equals("CORTE") || det.getProducto().getClave().equals("MANIOBRA"))
						continue;
					if(det.getPrecio().doubleValue()!=det.getPrecioOriginal().doubleValue()){
						if(logger.isDebugEnabled()){
							logger.debug("El pedido tiene al menos una partida con precio especial");
						}
						return "PRECIO ESPECIAL EN CREDITO ";
					}
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
