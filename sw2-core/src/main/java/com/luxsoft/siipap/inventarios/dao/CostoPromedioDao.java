package com.luxsoft.siipap.inventarios.dao;

import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.core.Producto;

public interface CostoPromedioDao extends GenericDao<CostoPromedio, Long>{
	
	
	
	public CostoPromedio buscar(final String producto,int year,int mes);
	
	public void eliminarCostoPromedio(int year,int mes);
	
	public void eliminarCostoPromedio(Producto p,int year,int mes);
	
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
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedio> buscarCostosPromedios(final String clave,final int year,int mes);
	
}
