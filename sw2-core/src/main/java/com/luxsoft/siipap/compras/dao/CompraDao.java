package com.luxsoft.siipap.compras.dao;

import java.util.List;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.CompraRow;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

public interface CompraDao extends GenericDao<Compra, Long>{
	
	
	
	/**
	 * Regresa una compra completamente inicializada
	 * 
	 * @param id
	 * @return
	 */
	public Compra inicializarCompra(final Long id);
	
	/**
	 * Busca una lista de compras para el period indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Compra> buscarCompras(Periodo p);
	
	public List<CompraRow> buscarComprasRow();
	
	/**
	 * Busca las compras pendientes para un proveedor en particular
	 * 
	 * @return
	 */
	public List<CompraDet> buscarComprasPendientesPorProveedor(final Proveedor p);	
	
	/**
	 * Permite localizar una compra con la llave usada en siipap dbf
	 * 
	 * @param sucursal
	 * @param folio
	 * @return
	 */
	public Compra buscarPorFolio(final int sucursal,int folio);
	
	
	

}
