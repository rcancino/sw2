package com.luxsoft.sw3.contabilidad.polizas;

import com.luxsoft.sw3.contabilidad.model.Poliza;

/**
 * Procesa una poliza para agregarle partidas segun 
 * alguna regla de negocios.
 * 
 * @author Ruben Cancino
 *
 * @param <T> La entidad utilizada para procesar 
 */
public interface Procesador <T>{
	
	/**
	 * Determina si la entidad actual debe y puede ser atendida por este procesador
	 * 
	 * @param entidad
	 * @return
	 */
	public boolean evaluar(T entidad,Poliza poliza);
	
	/**
	 * Procesa la  entidad para generar los registros contables relacionados con la misma
	 * 
	 * @param poliza
	 * @param entidad
	 */
	public void procesar(Poliza poliza,T entidad);

}
