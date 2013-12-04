package com.luxsoft.siipap.model.gastos;

/**
 * Implementacion de {@link GCXPFactory}
 * 
 * @author Ruben Cancino
 *
 */
@Deprecated
public class GCXPFactoryImpl implements GCXPFactory{

	public GCxP createCxP(final GCompra compra) {
		final GCxP cxp=new GCxP(compra);
		cxp.setTipo(GTipoDeMovimiento.CARGO);
		return cxp;
	}

}
