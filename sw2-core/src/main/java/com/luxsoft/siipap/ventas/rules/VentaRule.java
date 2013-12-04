package com.luxsoft.siipap.ventas.rules;

import java.util.Map;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Define las capacidades de una regla de negocios para una venta
 * El uso de esta regla es controlado por un tercero, la finalidad
 * de esta interfaz es aplicar una regla en especifico
 * 
 * 
 * @author Ruben Cancino
 *
 */
public interface VentaRule {
	
	public static final String TIPO_DE_VENTA_KEY="tipo.de.venta.key";
	
	/**
	 * Metodo detonado al momento de asignar el cliente
	 * a una venta
	 * 
	 * @param c
	 * @param v
	 * @param context
	 */
	public void asignarCliente(Cliente c,Venta v ,Map<String, Object> context);
	
	/**
	 * Callback method para implementar las reglas de negocios al momento
	 * de asignar el tipo de venta si es de credito o de contado
	 * 
	 * @param v
	 * @param context
	 */
	public void asignarTipoDeVenta(Venta v,Map<String,Object> context);

}
