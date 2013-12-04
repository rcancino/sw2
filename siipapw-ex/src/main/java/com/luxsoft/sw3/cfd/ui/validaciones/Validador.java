package com.luxsoft.sw3.cfd.ui.validaciones;

import java.io.File;
import java.util.List;

/**
 * Interface para la validacion de comprobantes fiscales digitales
 * 
 * @author Ruben Cancino
 *
 */
public interface Validador {
	
	/**
	 * Regresa una lista de los resultados de la validacion
	 * 
	 * @param xmlFile
	 * @return
	 */
	public List<Resultado> validar(File xmlFile);
	
	/**
	 * Regresa un string HTML que representa los resultados de la validacion
	 * 
	 * @return String en HTML de los resultados
	 */
	public String getHTMLResult();

}
