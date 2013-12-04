package com.luxsoft.sw3.cxc.consultas;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Bean q representa una cuenta por cobrar  
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CuentaPorCobrar {
	
	private String id; 
	private String tipo="FAC";
	private Long documento;
	private Currency moneda=MonedasUtils.PESOS;
	private double tc=1;
	private Date fecha;
	private int atraso;
	private String sucursal;
	private String clave;
	private String nombre;
	private BigDecimal total;
	private BigDecimal devoluciones;
	private BigDecimal bonificaciones;
	private BigDecimal pagos;
	private BigDecimal saldo;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Long getDocumento() {
		return documento;
	}
	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	
	
	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}
	public int getAtraso() {
		return atraso;
	}
	public void setAtraso(int atraso) {
		this.atraso = atraso;
	}
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getDevoluciones() {
		return devoluciones;
	}
	public void setDevoluciones(BigDecimal devoluciones) {
		this.devoluciones = devoluciones;
	}
	public BigDecimal getBonificaciones() {
		return bonificaciones;
	}
	public void setBonificaciones(BigDecimal bonificaciones) {
		this.bonificaciones = bonificaciones;
	}
	public BigDecimal getPagos() {
		return pagos;
	}
	public void setPagos(BigDecimal pagos) {
		this.pagos = pagos;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	
	
	public double getTc() {
		return tc;
	}
	public void setTc(double tc) {
		this.tc = tc;
	}
	
	public CantidadMonetaria getSaldoMN(){
		return CantidadMonetaria.pesos(getSaldo()).multiply(getTc());
	}
	

}
