package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;

/**
 * Analisis financiero de ventas
 * 
 * La regal de negocios para llegar a la venta neta es:
 * 	
 * 1  Obtener el importe de la venta a precio de lista
 * 2  Restar el Desceutno antes de CXPFactura (descuentoAF)
 * 	  y el resultado se convier
 *   
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisFV {
	
	private BigDecimal ventasPL;
	
	private BigDecimal ventasPF;
	
	private BigDecimal descuentoF;
	
	private BigDecimal servicios;
	
	private BigDecimal ventaSN;
	
	private BigDecimal provision;
	
	private BigDecimal descuentoNC;
	
	private BigDecimal devoluciones;
	
	private BigDecimal ventaNeta;
	
	private BigDecimal costo;
	
	private BigDecimal utilidad;
	
	private BigDecimal cargos;

	/**
	 * Ventas a precio de lista
	 * 
	 * @return
	 */
	public BigDecimal getVentasPL() {
		return ventasPL;
	}

	public void setVentasPL(BigDecimal ventasPL) {
		this.ventasPL = ventasPL;
	}
	
	/**
	 * Ventas a precio facturado
	 *  
	 * @return
	 */
	public BigDecimal getVentasPF() {
		return ventasPF;
	}

	public void setVentasPF(BigDecimal ventasFac) {
		this.ventasPF = ventasFac;
	}

	/**
	 * Importe del descuento aplicado al precio de lista
	 * 
	 * @return
	 */
	public BigDecimal getDescuentoAF() {
		BigDecimal dif=this.ventasPL.subtract(ventasPF);
		return dif;
	}

	/**
	 * Importe del descuento derivado de las reglas
	 * y politicas de descuentos. Este importe no
	 * esta respaldado por notas de credito, sino por
	 * las mismas facturas de ventas
	 * 
	 * @return
	 */
	public BigDecimal getDescuentoF() {
		return descuentoF;
	}

	public void setDescuentoF(BigDecimal descuentoF) {
		this.descuentoF = descuentoF;
	}

	/**
	 * Importe de los servicios asociados a las ventas
	 * como son los cortes,maniobras y fletes
	 * 
	 * @return
	 */
	public BigDecimal getServicios() {
		return servicios;
	}
	public void setServicios(BigDecimal servicios) {
		this.servicios = servicios;
	}

	/**
	 * Importe de las ventas menos los descuentos
	 * que no estan respaldados por notas de credito como
	 * son el descuentoF y el descuentoAF 
	 * 
	 * @return
	 */
	public BigDecimal getVentaSN() {
		return ventaSN;
	}

	public void setVentaSN(BigDecimal ventaSN) {
		this.ventaSN = ventaSN;
	}

	/**
	 * Importe de la provision de derivada de ventas
	 * a credito 
	 * 
	 * @return
	 */
	public BigDecimal getProvision() {
		return provision;
	}

	public void setProvision(BigDecimal provision) {
		this.provision = provision;
	}

	/**
	 * Importe de los descuentos expresados en notas de credito
	 * explicitamente declarados. Normalmente son notas de credito
	 * U,V y L
	 * 
	 * @return
	 */
	public BigDecimal getDescuentoNC() {
		return descuentoNC;
	}
	
	public void setDescuentoNC(BigDecimal descuentoNC) {
		this.descuentoNC = descuentoNC;
	}

	/**
	 * Importe de devoluciones expresadas en notas de credito
	 * 
	 * @return
	 */
	public BigDecimal getDevoluciones() {
		return devoluciones;
	}

	public void setDevoluciones(BigDecimal devoluciones) {
		this.devoluciones = devoluciones;
	}

	/**
	 * Importe de la venta neta
	 * 
	 * @return
	 */
	public BigDecimal getVentaNeta() {
		return ventaNeta;
	}

	public void setVentaNeta(BigDecimal ventaNeta) {
		this.ventaNeta = ventaNeta;
	}

	/**
	 * Importe del costo de la venta
	 * 
	 * @return
	 */
	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

	/**
	 * Utilidad bruta 
	 * 
	 * @return
	 */
	public BigDecimal getUtilidad() {
		return utilidad;
	}

	public void setUtilidad(BigDecimal utilidad) {
		this.utilidad = utilidad;
	}

	/**
	 * Importe de notas de cargo
	 * 
	 * @return
	 */
	public BigDecimal getCargos() {
		return cargos;
	}

	public void setCargos(BigDecimal cargos) {
		this.cargos = cargos;
	}
	
	

}
