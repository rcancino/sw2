package com.luxsoft.siipap.model;

import org.springframework.core.NestedRuntimeException;

/**
 * {@link RuntimeException} vinculada con problemas de autorizacion
 * de procedimientos. No autorizacion de seguridad del sistema
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class AutorizacionException extends NestedRuntimeException{

	public AutorizacionException(String msg) {
		super(msg);
	}

}
