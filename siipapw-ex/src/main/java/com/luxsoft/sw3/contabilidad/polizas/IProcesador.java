package com.luxsoft.sw3.contabilidad.polizas;

import org.springframework.ui.ModelMap;

import com.luxsoft.sw3.contabilidad.model.Poliza;

/**
 * Procesa una poliza para agregarle partidas segun 
 * alguna regla de negocios.
 * 
 * @author Ruben Cancino  
 */
public interface IProcesador{
	
	
	/**
	 * Procesa la  entidad para generar los registros contables relacionados con la misma
	 * La informacion requerida para esto se debe obtener del model.
	 * 
	 * @param poliza
	 * @param model
	 */
	public void procesar(Poliza poliza,ModelMap model);

}
