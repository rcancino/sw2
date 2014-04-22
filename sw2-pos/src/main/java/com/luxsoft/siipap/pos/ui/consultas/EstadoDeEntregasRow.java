package com.luxsoft.siipap.pos.ui.consultas;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.time.DurationFormatUtils;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.util.SQLUtils;

public class EstadoDeEntregasRow {
	
	 private String id;
	 private String origen;
	 private String cliente;
	 private Integer transporte;
	 private String chofer;
	 private Double factura;
	 private Double pedido;
	 private Date fechaFactura;
	 private Date asignado;
	 private Date salida;
	 private Date arribo;
	 private Date recepcion;
     private Double embarque;
     
     
     
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	public String getCliente() {
		return cliente;
	}
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	public Integer getTransporte() {
		return transporte;
	}
	public void setTransporte(Integer transporte) {
		this.transporte = transporte;
	}
	public String getChofer() {
		return chofer;
	}
	public void setChofer(String chofer) {
		this.chofer = chofer;
	}
	public Double getFactura() {
		return factura;
	}
	public void setFactura(Double factura) {
		this.factura = factura;
	}
	public Double getPedido() {
		return pedido;
	}
	public void setPedido(Double pedido) {
		this.pedido = pedido;
	}
	
	public Date getFechaFactura() {
		return fechaFactura;
	}
	public void setFechaFactura(Date fechaFactura) {
		this.fechaFactura = fechaFactura;
	}
	public Date getAsignado() {
		return asignado;
	}
	public void setAsignado(Date asignado) {
		this.asignado = asignado;
	}
	public Date getSalida() {
		return salida;
	}
	public void setSalida(Date salida) {
		this.salida = salida;
	}
	public Date getArribo() {
		return arribo;
	}
	public void setArribo(Date arribo) {
		this.arribo = arribo;
	}
	public Date getRecepcion() {
		return recepcion;
	}
	public void setRecepcion(Date recepcion) {
		this.recepcion = recepcion;
	}
	public Double getEmbarque() {
		return embarque;
	}
	public void setEmbarque(Double embarque) {
		this.embarque = embarque;
	}
     
}
