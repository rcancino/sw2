package com.luxsoft.sw3.cfd.model;

import org.springframework.core.NestedRuntimeException;

/**
 * Excepcion principal del sub sistema de CFD
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class CFDException extends NestedRuntimeException{

	public CFDException(String msg) {
		super(msg);
	}

	public CFDException(String msg, Throwable cause) {
		super(msg, cause);		
	}

	

}
