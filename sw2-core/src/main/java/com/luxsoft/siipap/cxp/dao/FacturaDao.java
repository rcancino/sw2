package com.luxsoft.siipap.cxp.dao;

import java.util.Currency;
import java.util.List;

import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

public interface FacturaDao extends GenericDao<CXPFactura, Long>{
	
	/**
	 * Busca una factura importada desde oracle
	 * 
	 * @param analisisId
	 * @return
	 */
	public CXPFactura buscarPorAnalisis(Long analisisId);
	
	/**
	 * Busca las facturas del periodo
	 * 
	 * @param p
	 * @return
	 */
	public List<CXPFactura> buscarFacturas(Periodo p) ;
	
	/**
	 * Localiza todas las facturas pendientes 
	 * 
	 * @return
	 */
	public List<CXPFactura> buscarFacturasPendientes();
	
	/**
	 * Busca las facturas relacionadas con el proveedor
	 * 
	 * @param proveedor
	 * @return
	 */
	public List<CXPFactura> buscarFacturas(final Proveedor proveedor);
	
	/**
	 * Regresa el detalle del analisis para la factura indicada
	 * 
	 * @param factura
	 * @return
	 */
	public List<CXPAnalisisDet> buscarAnalisis(final CXPFactura factura);
	
	
	/**
	 * Busca todas las facturas con pagos pendientes
	 * 
	 * @param proveedor
	 * @param moneda
	 * @return
	 */
	public List<CXPFactura> buscarFacturasPorRequisitar(final Proveedor proveedor,final Currency moneda);
	
	public List<CXPCargo> buscarCuentasPorPagar(Proveedor proveedor,Currency moneda);
	
	
	

}
