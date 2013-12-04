package com.luxsoft.siipap.model.tesoreria;

/**
 * Origenes de movimientos
 * 
 * @author Ruben Cancino
 *
 */
public enum Origen {
	
	VENTA_CREDITO
	,VENTA_CAMIONETA
	,VENTA_MOSTRADOR
	,CARGO_INICIAL
	,MOVIMIENTO_MANUAL
	,PAGO_GASTOS
	,VENTA_CONTADO
	,TESORERIA
	,COMPRAS
	,GASTOS
	,JUR
	,CHE;
	

	public String getName(){
		switch (this) {
		case VENTA_CAMIONETA:
			return "CAM";
		case VENTA_CREDITO:
			return "CRE";
		case VENTA_MOSTRADOR:
			return "MOS";
		case VENTA_CONTADO:
			return "CON";
		case CARGO_INICIAL:
			return "INI";
		case MOVIMIENTO_MANUAL:
			return "TES";
		case PAGO_GASTOS:
			return "GAS";
		case TESORERIA:
			return "TES";
		case COMPRAS:
			return "COM";
		case GASTOS:
			return "GAS";
		default:
			return name();
		}
	}
}
