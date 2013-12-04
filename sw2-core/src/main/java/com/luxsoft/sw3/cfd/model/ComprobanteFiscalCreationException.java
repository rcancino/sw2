package com.luxsoft.sw3.cfd.model;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception para indicar error en la generacion de comprobantes
 * fiscales digitales CFD
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComprobanteFiscalCreationException extends NestedRuntimeException{

	public ComprobanteFiscalCreationException(String msg) {
		super(msg);
	}

	public ComprobanteFiscalCreationException(String msg, Throwable cause) {
		super(msg, cause);		
	}

	

}
