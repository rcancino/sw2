package com.luxsoft.siipap.inventarios.service;

import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.GenericManager;

public interface TransformacionesManager extends GenericManager<TransformacionDet, String>{
	
	/**
	 * Persiste una  transformaciones que fue importada desde un bean {@link MovimientoDet}
	 * 
	 * Opera en una sola transaccion
	 * 	 Al persistir cada transformacion elimina el movimiento origen
	 * 
	 * @param data
	 * @return
	 */
	public TransformacionDet persistirImportacion(TransformacionDet data);
	
	
	/**
	 * Importa transformacioens pendientes desde SX_INVENTARIO_MOV
	 * 
	 * Nota: Por compatibilidad con SIIPAP DBF
	 * 
	 * @param periodo
	 */
	public void importarPendientes(final Periodo periodo);
	
	
	/**
	 * Actualiza el costo de las transformaciones
	 * Nota: Asume que el costo sera el costo promedio de la salida, por lo que debe 
	 * se asume que este debe existir o estar acutalizado antes de que este se aplique
	 * 
	 * @param periodo
	 * @param clave
	 */
	public void actualizarCostos(final Periodo periodo,String clave);
	
	/**
	 * Actualiza el costo de las transformaciones
	 * 
	 * @param year
	 * @param mes
	 */
	public void actualizarCostos(int year, int mes);
	
	/**
	 * Actualiza el costo de las transformaciones
	 * 
	 * @param year
	 * @param mes
	 * @param clave
	 */
	public void actualizarCostos(int year, int mes,String clave);

}
