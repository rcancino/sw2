package com.luxsoft.sw3.pedidos.forms;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import junitx.util.PrivateAccessor;


import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Una version limitada del cargo de un cliente
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidosPendientesRow implements Serializable{
	private Long folio;
	private Date fecha;
	private String clave;
	private String descripcion;
	private BigDecimal solicitado;
	private BigDecimal depurado;
	private Date depuracion;
	private BigDecimal entregado;
	private Date ultimaEntrada;
	private BigDecimal pendiente;
	
	
	
	
	public Long getFolio() {
		return folio;
	}
	public void setFolio(Long folio) {
		this.folio = folio;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public BigDecimal getSolicitado() {
		return solicitado;
	}
	public void setSolicitado(BigDecimal solicitado) {
		this.solicitado = solicitado;
	}
	public BigDecimal getDepurado() {
		return depurado;
	}
	public void setDepurado(BigDecimal depurado) {
		this.depurado = depurado;
	}
	public Date getDepuracion() {
		return depuracion;
	}
	public void setDepuracion(Date depuracion) {
		this.depuracion = depuracion;
	}
	public BigDecimal getEntregado() {
		return entregado;
	}
	public void setEntregado(BigDecimal entregado) {
		this.entregado = entregado;
	}
	public Date getUltimaEntrada() {
		return ultimaEntrada;
	}
	public void setUltimaEntrada(Date ultimaEntrada) {
		this.ultimaEntrada = ultimaEntrada;
	}
	public BigDecimal getPendiente() {
		return pendiente;
	}
	public void setPendiente(BigDecimal pendiente) {
		this.pendiente = pendiente;
	}
	
	
 

}
