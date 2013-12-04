package com.luxsoft.siipap.dao.gastos;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraRow;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;

public interface GCompraDao extends GenericDao<GCompra, Long>{
	
	public List<GCompra> buscarPorProveedor(final GProveedor p);
	
	
	/**
	 * Busca todas las compras pendientes de requisición para un proveedor
	 * 
	 * @param p
	 * @return
	 */
	public List<GCompra> buscarPendientesDeRequisicion(final GProveedor p);
	
	/**
	 * Regresa la lista de todas las compras en formato {@link GCompraRow}
	 * 
	 * @return
	 */
	public List<GCompraRow> buscarComprasRow();
	
	/**
	 * Regresa la lista de todas las compras en formato {@link GCompraRow}
	 * para el periodo indicado
	 * @param p
	 * @return
	 */
	public List<GCompraRow> buscarComprasRow(Periodo p);
	
	
	/**
	 * Acumula en un rubro el total de gastos en un mes,sucursal,año 
	 *
	 */
	public Map<ConceptoDeGasto,BigDecimal> acumularGastos(final String rubroClave,int year,int mes,int sucursal);
	
	
	
	
	/**
	 * Regresa una lista de todas las facturas registradas
	 * 
	 * @return
	 */
	public List<GFacturaPorCompra> buscarFacturas();
	
	/**
	 * Regresa una lista de todas las facturas registradas
	 * para el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<GFacturaPorCompra> buscarFacturas(Periodo p);
	
	public GFacturaPorCompra buscarFactura(final Long id);
	
	public GFacturaPorCompra salvarFactura(final GFacturaPorCompra fac);
	
	/**
	 * Busca las facturas por fecha de la mimsa
	 * 
	 * @param fecha
	 * @return
	 */
	public List<GFacturaPorCompra> buscarFacturas(final Date fecha);
	
	/**
	 * Regresa las facturas con saldo para el periodo indicado. El saldo se
	 * considera no el saldo actual sino el saldo a la fecha final del periodo indicado
	 * 
	 * @param periodo
	 * @return
	 */
	public List<GFacturaPorCompra> buscarFacturasConstaldo(final Periodo periodo);
	
	/**
	 * Busca las facturas por fecha contable
	 * 
	 * @param fecha
	 * @return
	 */
	public List<GFacturaPorCompra> buscarFacturasFechaContable(final Date fecha);
	
}
