package com.luxsoft.siipap.compras.dao;

import java.util.List;


import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

public interface Compra2Dao extends GenericDao<Compra2, String>{
	
	/**
	 * Regresa una compra completamente inicializada
	 * 
	 * @param id
	 * @return
	 */
	public Compra2 inicializarCompra(final String id);
	
	/**
	 * Busca una lista de compras para el period indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Compra2> buscarCompras(Periodo p);
	
	
	
	/**
	 * Busca las compras pendientes para un proveedor en particular
	 * 
	 * @return
	 */
	public List<CompraUnitaria> buscarComprasPendientesPorProveedor(final Proveedor p);	
	
	/**
	 * Permite localizar una compra con la llave usada en siipap dbf
	 * 
	 * @param sucursal
	 * @param folio
	 * @return
	 */
	public Compra2 buscarPorFolio(final int sucursal,int folio);
	
	/**
	 * Comoditi para traer solamente las compras unitarias
	 * 
	 * @param c
	 * @return
	 */
	public List<CompraUnitaria> buscarPartidas(final Compra2 c);
	
	public List<Compra2> buscarPendientes();
	
	

}
