package com.luxsoft.siipap.ventas.rules;

/**
 * Error relacionado con la falla en una regla de negocios
 * para las operaciones de ventas
 * 
 * @author Ruben Cancino
 *
 */
public class VentaRuleException extends RuntimeException{

	public VentaRuleException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public VentaRuleException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public VentaRuleException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public VentaRuleException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	

}
