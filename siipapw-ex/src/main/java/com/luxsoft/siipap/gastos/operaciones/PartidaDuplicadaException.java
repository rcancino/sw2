package com.luxsoft.siipap.gastos.operaciones;

import org.springframework.core.NestedRuntimeException;

/**
 * Error no fatal para indicar que una partida ya se a registrado
 * 
 * @author Ruben Cancino
 *
 */
public class PartidaDuplicadaException extends NestedRuntimeException{

	public PartidaDuplicadaException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

}
