package com.luxsoft.siipap.ventas.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AnalisisDeVenta implements Comparable<AnalisisDeVenta>{
	
	private String linea;
	private String sucursal;
	private String tipo;	
	private double ventaBruta;
	private double descuento;
	private double utilidad;
	private Integer mes;
	
	public String getLinea() {
		return linea;
	}
	public void setLinea(String linea) {
		this.linea = linea;
	}
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Integer getMes() {
		return mes;
	}
	public void setMes(Integer mes) {
		this.mes = mes;
	}
	public double getVentaBruta() {
		return ventaBruta;
	}
	public void setVentaBruta(double ventaBruta) {
		this.ventaBruta = ventaBruta;
	}
	public double getDescuento() {
		return descuento;
	}
	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}
	public double getUtilidad() {
		return utilidad;
	}
	public void setUtilidad(double utilidad) {
		this.utilidad = utilidad;
	}
	
	public int compareTo(AnalisisDeVenta o) {
		return mes.compareTo(o.getMes());
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
		
	}
	
	

}
