package com.luxsoft.sw3.implanta;

import com.luxsoft.siipap.ventas.model.AutorizacionParaFacturarSinExistencia;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.PedidoPendiente;

/**
 * Elimina todos los registros de operacion para las entidades relacionadas con
 * pedidos
 * 		
 *    
 *    	PedidoDet
 *		AutorizacionDePedido autorizacion
 *		AutorizacionDePedido pagoContraEntrega
 *		InstruccionDeEntrega instruccionDeEntrega;
 *		AutorizacionParaFacturarSinExistencia autorizacionSinExistencia;
 *		PedidoPendiente pendiente    
 *      Pedido
 *      
 *       
 *       
 * @author ruben
 *
 */
public class DepurarPedidos {
	
	
	
	/**
	 * Depura la base de datos de
	 * 
	 * @param sucursalId
	 */
	public void depurar(long sucursalId){
		
	}
	
	private void eliminarPedidosDet(long sucursalId){
		
	}

}
