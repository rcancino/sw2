package com.luxsoft.siipap.model.tesoreria;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception para las opreaciones realcionadas con Transferencias
 * 
 *  
 * @author Ruben Cancino
 *
 */
public class TransferenciaException extends NestedRuntimeException{

	public TransferenciaException(String msg) {
		super(msg);
	}

}
