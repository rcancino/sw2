package com.luxsoft.siipap.ventas.service;

/**
 * Esception arrojada cuando existen errores con los descuentos
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DescuentoException extends RuntimeException{
	
	public DescuentoException(String message) {
		super(message);
		
	}
	

}
