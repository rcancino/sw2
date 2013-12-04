package com.luxsoft.sw3.ventas.rules;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;

/**
 * Procesador de pedido para asignar las comisiones por el uso de cheque post fechado
 * 
 * @author Ruben Cancino Ramos
 *@deprecated NO USAR MAS: LA REGLA DE NEGOCIOS SE INSERTA EN EL Pedido
 */
@Component("procesadorDeComisionPorCheque")
public class ProcesadorDeComisionPorChequePostFechado implements PedidoProcessor{
	
	protected Logger logger=Logger.getLogger(getClass());

	public void process(final Pedido pedido) {
		if(pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
			BigDecimal comision=calcularComisionPorChequePostFechado(pedido.getPartidas());
			pedido.setComisionTarjetaImporte(comision);
			//pedido.actualizarComisiones();
			BigDecimal importeBruto=pedido.getImporteBruto();
			if(importeBruto.doubleValue()>0){
				double com=(comision.doubleValue()/importeBruto.doubleValue())*100;
				pedido.setComisionTarjeta(com);
			}
			pedido.setComentarioComision(FormaDePago.CHEQUE_POSTFECHADO.name());
		}
		
	}
	
	/**
	 * Calcula el monto total de la comision en funcion de las partidas del
	 * pedido
	 * 
	 * @return
	 */
	private BigDecimal calcularComisionPorChequePostFechado(final Collection<PedidoDet> partidasSource){
		BigDecimal comisionTotal=BigDecimal.ZERO;
		
		for(PedidoDet det:partidasSource){
			BigDecimal comision;
			if(det.getProducto().getModoDeVenta().endsWith("B")){
				comision=BigDecimal.valueOf(.04d);
			}else
				comision=BigDecimal.valueOf(.02d);
			comision=det.getImporteBruto().multiply(comision);
			comisionTotal=comisionTotal.add(comision);
		}
		return comisionTotal;
	}

}
