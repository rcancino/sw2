package com.luxsoft.sw3.cfd.services;

import java.security.cert.X509Certificate;



/**
 * Service Manager para generar el sello digital de los comprobantes fiscales digitales
 * CFD. Tomando como base la cadena original
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface SelladorDigital {
	
	public static String[] ALGORITMOS={"MD5withRSA"};
	
	/**
	 * Sella la cadena original usando el algoritmo RSA y regresa el sello (la firma) 
	 * codificado en el estandar Base64 
	 *  
	 * 	  
	 * @param cadenaOrignal
	 * @return
	 */
	public String getSello(final String cadenaOrignal) ;
	
	/**
	 * Regresa el certificado digital con el que se genera el sello
	 * digital
	 * 
	 * @return
	 */
	public X509Certificate getCertificado();
	
	/**
	 * Valida que una cadena y su sello digital
	 * 
	 * @param cadenaOriginal
	 * @param selloDigital
	 * @return
	 */
	public boolean validar(final String cadenaOriginal,final String selloDigital);
	
	
		
	

}
