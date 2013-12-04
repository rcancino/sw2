package com.luxsoft.siipap.service.gastos;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraRow;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.service.GenericManager;

public interface ComprasDeGastosManager extends GenericManager<GCompra, Long>{
	
	
	
	
	/**
	 * Regresa la lista de todas las compras en formato {@link GCompraRow}
	 * 
	 * @return
	 */
	public List<GCompraRow> buscarComprasRow();
	
	/**
	 * Regresa la lista de todas las compras en formato {@link GCompraRow}
	 * para el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<GCompraRow> buscarComprasRow(Periodo p);
	
	/**
	 * Genera la poliza de gastos para las facturas recibidas
	 * en la fecha indicada
	 * 
	 * @param periodo
	 * @return
	 */
	public Poliza generarPolizaDeProvision(final Periodo periodo);
	
	/**
	 * Genera la lista de poilizas de pago para la fecha indicada
	 * @param fecha
	 * @return
	 */
	public List<Poliza> generarPolizaDePagos(final Date fecha);
	
	/**
	 * Genera la lista de poilizas de pago para la fecha indicada
	 * @param fecha
	 * @return
	 */
	public List<Poliza> generarPolizaDePagos2(final Date fecha);
	
	
	public GFacturaPorCompra salvarFactura(final GFacturaPorCompra fac);
	
	
	
	
	

}
