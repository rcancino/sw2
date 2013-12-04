package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;

/**
 * Bean util durante el proceso de migracion 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class OracleCargo {
	
	private Long id;
	
	private String serie;
	private String tipo;
	private int sucursal;
	private Long documento;
	private BigDecimal total;
	private BigDecimal saldo;
	private BigDecimal descuentos;
	private BigDecimal bonificaciones;
	private BigDecimal devoluciones;
	private double descuento;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public int getSucursal() {
		return sucursal;
	}
	public void setSucursal(int sucursal) {
		this.sucursal = sucursal;
	}
	public Long getDocumento() {
		return documento;
	}
	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	public BigDecimal getDescuentos() {
		return descuentos;
	}
	public void setDescuentos(BigDecimal descuentos) {
		this.descuentos = descuentos;
	}
	public BigDecimal getBonificaciones() {
		return bonificaciones;
	}
	public void setBonificaciones(BigDecimal bonificaciones) {
		this.bonificaciones = bonificaciones;
	}
	public BigDecimal getDevoluciones() {
		return devoluciones;
	}
	public void setDevoluciones(BigDecimal devoluciones) {
		this.devoluciones = devoluciones;
	}
	public double getDescuento() {
		return descuento;
	}
	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}
	
	

}
