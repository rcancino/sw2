package com.luxsoft.siipap.cxc.model;

public interface NotaRules {

	/**
	 * Actualiza el importe de las notas de credito
	 * @param notaCredito
	 */
	public abstract void actualizarImportes(final NotaDeCredito notaCredito);

	/**
	 * Calcula el importe de las aplicaciones en funcion
	 * de el total de la cuenta por pagar y el descuento global
	 * de la nota.
	 *   
	 * Se asume que las aplicaciones se hacen sobre
	 * el total (con impuesto) de la cuenta x cobrar  
	 * 
	 */
	public abstract void aplicarDescuento(final NotaDeCredito nota);

	/**
	 * Actualiza el importe, impuesto y el total de la nota en funcion del
	 * importe de las aplicaciones
	 * 
	 * Muy util en algunos tipos de notas como las de bonificacion sobre
	 * ventas
	 *  
	 */
	public abstract void actualizarImportesDesdeAplicaciones(final NotaDeCredito nota);

}