package com.luxsoft.siipap.cxc.model;

/**
 * Formas de pago disponibles
 * 
 * @author Ruben Cancino
 * 
 *
*/
public enum FormaDePago {
	
	EFECTIVO,
	TARJETA_CREDITO,
	TARJETA_DEBITO,
	TARJETA,
	DEPOSITO,
	TRANSFERENCIA,
	CHEQUE,
	CHEQUE_POSTFECHADO,
	CHECKPLUS;
	
	public static FormaDePago[] getFormasValidas(){
		return new FormaDePago[]{EFECTIVO,
				TARJETA_CREDITO,
				TARJETA_DEBITO,
				DEPOSITO,
				TRANSFERENCIA,
				CHEQUE,
				CHEQUE_POSTFECHADO,
				CHECKPLUS};
	}
	
}