package com.luxsoft.sw3.cfd.services;

import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public interface ComprobantesDigitalesManager extends CFDSupport{
	
	/**
	 * Carga el comprobante fiscal para la venta especifica
	 * 
	 * @param venta
	 * @return
	 */
	public ComprobanteFiscal cargarComprobante(final Venta venta);
	
	/**
	 * Genera una instancia de CFD a partir de una venta
	 * 
	 * @param venta
	 * @return
	 */
	public ComprobanteFiscal generarComprobante(final Venta venta);
	
	/**
	 * Cancela el CFD asociado a la venta indicada
	 * 
	 * @param venta
	 * @return
	 */
	public ComprobanteFiscal cancelarComprobante(final Venta venta);
	
	/**
	 * 
	 * @param venta
	 * @return
	 */
	public ComprobanteFiscal generarComprobante(final NotaDeCredito venta);
	
	public ComprobanteFiscal cargarComprobante(final NotaDeCredito cargo);
	
	/**
	 * 
	 * @param notaDeCargo
	 * @return
	 */
	public ComprobanteFiscal generarComprobante(final NotaDeCargo notaDeCargo);
	
	public ComprobanteFiscal cargarComprobante(final NotaDeCargo cargo);

}
