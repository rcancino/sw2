package com.luxsoft.siipap.model;

import java.util.Properties;

/**
 * Define una estrategia para obtener las propiedades
 * de construccion de un modulo
 * 
 * @author Ruben Cancino
 *
 */
public interface ModuloPropertiesReader {
	
	/**
	 * Registra el modulo  
	 * 
	 * @param modulo
	 */
	public void setModulo(final Modulo moduloClave);
	
	/**
	 * Obtiene las propiedades especificas para un modulo 
	 * 
	 * @return El Properties bean para el modulo
	 */
	public Properties readProperties();

}
