package com.luxsoft.siipap.model.contabilidad;


/**
 * 
 * Interfaz central para generar polizas exportables a Contabilidad
 * En funcion de una regla especifica de negocios
 * 
 * @author Ruben Cancino
 *
 */
public interface GeneradorDePoliza {
	
	/**
	 * Genera una poliza en funcion de los parametros
	 * indicados
	 * 
	 * @param params
	 * @return
	 */
	public Poliza generar(Object...objects);
	
	

}
