package com.luxsoft.sw3.bi;

import java.math.BigDecimal;

public class AnalisisDeVenta {
		
	private String entidad;
	
	private String descripcion;
	
	private int year;
	
	private int mes;
	
	private long facturas;
	
	private BigDecimal ventasBrutas=BigDecimal.ZERO;
	
	private BigDecimal descuentos=BigDecimal.ZERO;
	
	private BigDecimal cargos=BigDecimal.ZERO;
	
	private BigDecimal importe=BigDecimal.ZERO;
	
	private BigDecimal impuesto=BigDecimal.ZERO;
	
	private BigDecimal total=BigDecimal.ZERO;
	
	private BigDecimal devoluciones=BigDecimal.ZERO;
	
	private BigDecimal bonificaciones=BigDecimal.ZERO;
	
	private BigDecimal costo=BigDecimal.ZERO;
	
	private BigDecimal ventaAcumulada=BigDecimal.ZERO;
	
	private BigDecimal utilidadAcumulada=BigDecimal.ZERO;
	
	private double kilos;

	

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	

	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public long getFacturas() {
		return facturas;
	}

	public void setFacturas(long facturas) {
		this.facturas = facturas;
	}

	public BigDecimal getVentasBrutas() {
		return ventasBrutas;
	}

	public void setVentasBrutas(BigDecimal ventasBrutas) {
		this.ventasBrutas = ventasBrutas;
	}

	public BigDecimal getDescuentos() {
		return descuentos;
	}

	public void setDescuentos(BigDecimal descuentos) {
		this.descuentos = descuentos;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

	public BigDecimal getCargos() {
		return cargos;
	}

	public void setCargos(BigDecimal cargos) {
		this.cargos = cargos;
	}

	
	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(BigDecimal impuesto) {
		this.impuesto = impuesto;
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

	

	public BigDecimal getCosto() {
		return costo;
	}

	public double getKilos() {
		return kilos;
	}

	public void setKilos(double kilos) {
		this.kilos = kilos;
	}	
	
	
	public BigDecimal getVentasNetas(){
		return getImporte().subtract(getDevoluciones()).subtract(getBonificaciones());
	}
	
	public BigDecimal getUtilidad(){
		return getVentasNetas().subtract(getCosto());
	}
	
	

	public BigDecimal getVentaAcumulada() {
		return ventaAcumulada;
	}

	public void setVentaAcumulada(BigDecimal ventaAcumulada) {
		this.ventaAcumulada = ventaAcumulada;
	}

	public BigDecimal getUtilidadAcumulada() {
		return utilidadAcumulada;
	}

	public void setUtilidadAcumulada(BigDecimal utilidadAcumulado) {
		this.utilidadAcumulada = utilidadAcumulado;
	}

	public double getParticipacion(){
		if(getVentaAcumulada().doubleValue()<=0)
			return 0;
		return (getVentasNetas().doubleValue()/getVentaAcumulada().doubleValue())*100;
	}
	
	public double getParticipacionUtilidad(){
		if(getUtilidadAcumulada().doubleValue()<=0)
			return 0;
		return (getUtilidad().doubleValue()/getUtilidadAcumulada().doubleValue())*100;
	}
	
	public double getUtitlidadPorcentual(){
		double util=getUtilidad().doubleValue();
		double net=getVentasNetas().doubleValue();
		if(net<=0)
			return 0;
		return (util/net)*100;
	}


}
