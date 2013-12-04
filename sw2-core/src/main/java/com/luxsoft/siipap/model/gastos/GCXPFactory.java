package com.luxsoft.siipap.model.gastos;

/**
 * FactoryMethods para la generacion de instancias GCxP
 * 
 * @author Ruben Cancino
 *
 */
@Deprecated
public interface GCXPFactory {
	
	/**
	 * Genera el cargo inicial relacionado con una compra
	 * 
	 * @param compra
	 * @return
	 */
	public GCxP createCxP(final GCompra compra);

}
