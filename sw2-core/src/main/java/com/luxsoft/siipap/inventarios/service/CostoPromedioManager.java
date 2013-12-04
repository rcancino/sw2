package com.luxsoft.siipap.inventarios.service;

import java.util.List;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.inventarios.model.CostoPromedioItem;
import com.luxsoft.siipap.service.GenericManager;

public interface CostoPromedioManager extends GenericManager<CostoPromedio, Long>{
	
	
	/**
	 * Localiza el CostoPromedio ara un artuiculo
	 * 
	 * @param year
	 * @param mes
	 * @param clave
	 * @return
	 */
	public CostoPromedio buscarCostoPromedio(final int year,final int mes,String clave);
	

	/**
	 * Actualiza el costo promedio para un articulo en particular
	 * 
	 * @param year
	 * @param mes
	 * @param clave
	 */
	public void actualizarCostoPromedio(final int year,final int mes,String clave);
	
	
	/**
	 * Verifica que una vez que exita costo promedio en un articulo este se
	 * traslade a meses <b>POSTERIORES</b> que no tengan costo</p>
	 * 
	 *  <p>Nota por limitaciones en la implementacion del procedimiento de calculo de costo promedio
	 *  este pequeño parche/ajuste es requerido para evitar que algunos movimientos (Especialmente ventas)
	 *  queden sin costo en algun mes <b>POSTERIOR</b> al primer costo obtenido. (Ocurre normalmente en medidas especiales)</p>
	 *   
	 * 
	 * @param year
	 */
	public void forwardCosto(final int year);
	
	/**
	 * Verifica que una vez que exita costo promedio en un articulo este se
	 * traslade a meses <b>ANTERIORES</b> que no tengan costo
	 * 
	 *  <p>Nota por limitaciones en la implementacion del procedimiento de calculo de costo promedio
	 *  este pequeño parche/ajuste es requerido para evitar que algunos movimientos (Especialmente ventas)
	 *  queden sin costo en algun mes <b>ANTERIOR</b> al primer costo obtenido. (Ocurre normalmente en medidas especiales)</P>
	 *   
	 * 
	 * @param year
	 */
	public void backwardCosto(final int year);
	
	/**
	 * Localiza el costo promedio para todos los productos en el periodo indicado
	 * 
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedio> buscarCostosPromedios(final int year,int mes);
	
	/**
	 * Localiza el costo promedio para el producto en el periodo indicado
	 * 
	 * @param clave
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedio> buscarCostosPromedios(final String clave,final int year,int mes);
	
	
	/**
	 * Busca los CostoPromedioItem
	 * 
	 * @param clave
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedioItem> buscarCostoItems(final String clave,final int year, final int mes);
	
}
