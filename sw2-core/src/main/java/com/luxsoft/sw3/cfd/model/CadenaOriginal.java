package com.luxsoft.sw3.cfd.model;

import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;

/**
 * Clase para encapsular la generación de una cadena original con base
 * a los lineamientos del SAT
 * 
 * @author Ruben Cancino
 *
 */
public class CadenaOriginal {
	
	private final Comprobante cfd;
	
	public CadenaOriginal(Comprobante cfd){
		this.cfd=cfd;
	}
	
	/**
	 * 
	 */
	public void procesar(){
		
	}
	
	public String getMessage(){
		return "";
	}

}
