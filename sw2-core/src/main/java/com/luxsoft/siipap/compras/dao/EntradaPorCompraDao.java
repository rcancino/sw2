package com.luxsoft.siipap.compras.dao;

import java.util.List;

import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

public interface EntradaPorCompraDao extends GenericDao<EntradaPorCompra, String>{
	
	/**
	 * Regresa una lista de compradet para
	 * 
	 * @param compraIds Id's de las compras requeridas
	 * @return
	 */
	public List<CompraDet> buscarEntradas(Long...compraIds);
	
	/**
	 * Busca las entradas por compra correspondientes al periodo
	 * indicado
	 * 
	 * @param periodo
	 * @return
	 */
	public List<EntradaPorCompra> buscarEntradas(final Periodo periodo);
	
	/**
	 * Busca una entrada por el COM_ID de SW_COMS2 de SiipapWin
	 * 
	 * @param comId
	 * @return
	 */
	public EntradaPorCompra buscarEntrada(Long comId);
	
	public List<EntradaPorCompra> buscarAnalisisPendientes(final Proveedor proveedor,final Periodo periodo);

}
