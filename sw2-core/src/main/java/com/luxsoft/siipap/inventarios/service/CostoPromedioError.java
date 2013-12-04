/**
 * 
 */
package com.luxsoft.siipap.inventarios.service;

import org.springframework.core.NestedRuntimeException;

public  class CostoPromedioError extends NestedRuntimeException{
	
	private String clave;
	private int year;
	private int mes;
	
	public CostoPromedioError(Throwable tx,String clave, int year, int mes) {
		super("Error calculando el costo promedio", tx);
		this.clave = clave;
		this.year = year;
		this.mes = mes;
	}
	
	public String getClave() {
		return clave;
	}
	
	public int getYear() {
		return year;
	}
	
	public int getMes() {
		return mes;
	}
		
}