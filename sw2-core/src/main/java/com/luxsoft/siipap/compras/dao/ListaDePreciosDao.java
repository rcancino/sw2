package com.luxsoft.siipap.compras.dao;

import java.util.Currency;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;

public interface ListaDePreciosDao extends GenericDao<ListaDePrecios, Long>{
	
	/**
	 * Regresa una lista de las listas de precios vigentes
	 * 
	 * @return
	 */
	public List<ListaDePrecios> buscarListasVigentes();
	
	/**
	 * Busca la lista de precios vigente
	 * 
	 * @param p
	 * @return
	 */
	public ListaDePrecios buscarListaVigente(Proveedor p);
	
	/**
	 * Regresa la lista de precios para el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<ListaDePrecios> buscarListas(final Periodo p);
	
	
	/**
	 * Hace una copia de la lista de precios 
	 * 
	 * @param id
	 * @return
	 */
	public ListaDePrecios copiar(Long id);
	
	/**
	 * Busca el precio de lista vigente y mas adecuado en las listas del proveedor
	 * 
	 * @param p
	 * @param prov
	 * @return
	 */
	//public ListaDePreciosDet buscarPrecioVigente(final Producto p,final Proveedor prov);
	
	/**
	 * 
	 * @param p
	 * @param moneda
	 * @param prov
	 * @param fecha
	 * @return
	 */
	public ListaDePreciosDet buscarPrecioVigente(final Producto p,final Currency moneda,final Proveedor prov,final Date fecha);
	
	/**
	 * 
	 * @param p
	 * @param prov
	 * @param fecha
	 * @return
	 */
	public ListaDePreciosDet buscarPrecioVigente(final Producto p,final Proveedor prov,final Date fecha);
	
	/**
	 * 
	 * @param claveProd
	 * @param fecha
	 * @return
	 */
	public List<ListaDePreciosDet> buscarPreciosVigentes(final String claveProd,final Date fecha);

}
