package com.luxsoft.siipap.cxp.service;

import java.util.Currency;
import java.util.List;

import com.luxsoft.siipap.cxp.dao.FacturaDao;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.GenericManager;

public interface FacturaManager extends GenericManager<CXPFactura, Long>{
	
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
	
	public List<CXPCargo> buscarCuentasPorPagar(final Proveedor proveedor,final Currency moneda);
	
	public CXPFactura buscarFactura(final String numero,final Proveedor p);
	
	public List<CXPFactura> buscarFacturas(final List<String> numeros,final Proveedor p);
	
	
	
	public FacturaDao getFacturaDao();
	
	public CXPFactura registrarDiferencia(CXPFactura fac);
	
	public void actualizarSaldo(CXPCargo fac);

}
