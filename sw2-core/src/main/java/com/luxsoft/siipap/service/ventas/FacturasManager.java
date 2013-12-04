package com.luxsoft.siipap.service.ventas;

import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AutorizacionParaCargo;
import com.luxsoft.siipap.cxc.model.CancelacionDeCargo;
import com.luxsoft.siipap.ventas.dao.VentaDao;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Manager especifico para la adminsitracion de facturas en POS
 * 
 * @author Ruben Cancino
 *
 */
public interface FacturasManager {
	
	
	public Venta getFactura(String id);
	
	public Venta buscarVentaInicializada(final String ventaId);
	
	public List<Aplicacion> buscarAplicaciones(final String ventaId);
	
	public Venta cancelarFactura(final String id,final Date fecha);
	
	public Venta cancelarFactura(CancelacionDeCargo cancelacion,final String id,final Date fecha);
	
	/**
	 * Regresa una lista de facturas sin persistir correspondientes al pedido
	 * 
	 * @param pedido
	 * @return
	 */
	public List<Venta> prepararParaFacturar(final Pedido pedido,final Date fecha);
	
	/**
	 * Persiste una lista de facturas 
	 * 
	 * @param facturas
	 * @return
	 */
	public List<Venta> facturar(final List<Venta> facturas);
	
	
	
	
	/**
	 * Factura un grupo de facturas y aplica los pagos correspondientes
	 *  
	 * @param pagos
	 * @param facturas
	 * @return
	 */
	public List<Venta> facturarYAplicar(final List<Abono> pagos,final List<Venta> facturas);
	
	/**
	 * Genera un abono autmaic para facturas con saldo <=1 peso
	 * 
	 * @param facturas
	 * 
	 */
	public void generarAbonoAutmatico(final List<Venta> facturas);
	
	
	/**
	 * Regresa el siguiente folio fiscal adecuado para la facturacion
	 *  
	 * @param pedido
	 * @return El siguiente folio fiscal
	 */
	//public long buscarFolioDeFactura(final Pedido pedido);
	
	
	public VentaDao getVentaDao();

}
