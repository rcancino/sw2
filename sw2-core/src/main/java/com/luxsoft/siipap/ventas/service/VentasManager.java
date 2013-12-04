package com.luxsoft.siipap.ventas.service;

import java.util.List;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;


/**
 * Manager para el acceso a todos los servicios
 * relacionados con las ventas
 *  
 * @author Ruben Cancino
 *
 */
public interface VentasManager {
	
	
	public Venta get(String id);
	
	public Venta buscarVentaInicializada(final String ventaId);
	
	public NotaDeCargo buscarNtaDeCargoInicializada(final String cargoId);
	
	public List<Aplicacion> buscarAplicaciones(final String ventaId);
	
	
	/**
	 * Persiste o actualiza  una venta
	 * 
	 * @param venta
	 * @return
	 */
	public Venta salvar(Venta venta);
	
	/**
	 * Ventas menos devoluciones para el periodo indicado
	 *   
	 *   Venta.importe - Devolucion.importe
	 *   
	 * @param p
	 * @param c
	 * @return
	 */
	public double getVolumenDeVentas(final Periodo p,Cliente c);
	

}
