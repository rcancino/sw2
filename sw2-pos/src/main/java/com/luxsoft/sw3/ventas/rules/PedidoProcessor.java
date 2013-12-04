package com.luxsoft.sw3.ventas.rules;

import com.luxsoft.sw3.ventas.Pedido;

/**
 * Interfaz para delegar el procesamiento de pedidos
 * 
 * Se asume que el pedido a procesar esta en un estado consistente y correcto
 * El uso de implementaciones de esta interface esta orientado a decorar y/o ajustar
 * un pedido
 *  
 *  Ejemplo de caso de uso
 *    
 *    La generacion de comisiones por la forma de pago 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface PedidoProcessor {
	
	
	public void process(final Pedido pedido);

}
