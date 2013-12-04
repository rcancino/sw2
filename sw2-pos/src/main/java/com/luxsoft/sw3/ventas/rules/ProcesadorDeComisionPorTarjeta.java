package com.luxsoft.sw3.ventas.rules;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

//import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Procesador de pedido para asignar las comisiones por el uso de cheque post fechado
 * 
 * @author Ruben Cancino Ramos
 * @deprecated LA REGLA DE NEGOCIOS PARA EL CARGO POR USO DE TARJETA NO PUEDE IMPLEMENTARCE
 *             MEDIANTE UN PEDIDO PROCESSOR
 */
@Component("procesadorDeComisionPorTarjeta")
public class ProcesadorDeComisionPorTarjeta implements PedidoProcessor{
	
	protected Logger logger=Logger.getLogger(getClass());

	public void process(final Pedido pedido) {
		if(pedido.getFormaDePago().name().startsWith("TARJETA")){
			/*Tarjeta t=pedido.getTarjeta();
			if(t!=null){
				double comision=t.getComisionVenta();
				pedido.setComisionTarjeta(comision);
				pedido.setComentarioComision(pedido.getFormaDePago().name());
				pedido.actualizarImportes();
			}*/
		}
		
	}
	
	

}
