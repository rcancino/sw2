package com.luxsoft.sw3.bi;

import java.math.BigDecimal;

public class BIVentasPorCliente {
		
	private long cliente_id;
	
	private int year;
	
	private int mes;
	
	private long ventas;
	
	private BigDecimal importeBruto;
	
	private double descuentos;
	
	private BigDecimal cargos;
	
	private BigDecimal flete;
	
	private BigDecimal importe;
	
	private BigDecimal impuesto;
	
	private BigDecimal total;
	
	private BigDecimal devoluciones;
	
	private BigDecimal bonificaciones;
	
	private double costo;
	
	private double kilos;

	public long getCliente_id() {
		return cliente_id;
	}

	public void setCliente_id(long cliente_id) {
		this.cliente_id = cliente_id;
	}

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

	public long getVentas() {
		return ventas;
	}

	public void setVentas(long ventas) {
		this.ventas = ventas;
	}

	public BigDecimal getImporteBruto() {
		return importeBruto;
	}

	public void setImporteBruto(BigDecimal importeBruto) {
		this.importeBruto = importeBruto;
	}

	public double getDescuentos() {
		return descuentos;
	}

	public void setDescuentos(double descuentos) {
		this.descuentos = descuentos;
	}

	public BigDecimal getCargos() {
		return cargos;
	}

	public void setCargos(BigDecimal cargos) {
		this.cargos = cargos;
	}

	public BigDecimal getFlete() {
		return flete;
	}

	public void setFlete(BigDecimal flete) {
		this.flete = flete;
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

	public double getCosto() {
		return costo;
	}

	public void setCosto(double costo) {
		this.costo = costo;
	}

	public double getKilos() {
		return kilos;
	}

	public void setKilos(double kilos) {
		this.kilos = kilos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cliente_id ^ (cliente_id >>> 32));
		result = prime * result + mes;
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BIVentasPorCliente other = (BIVentasPorCliente) obj;
		if (cliente_id != other.cliente_id)
			return false;
		if (mes != other.mes)
			return false;
		if (year != other.year)
			return false;
		return true;
	}
	
	private String clienteNombre;

	public String getClienteNombre() {
		return clienteNombre;
	}

	public void setClienteNombre(String clienteNombre) {
		this.clienteNombre = clienteNombre;
	}
	
	public BigDecimal getImporteNeto(){
		return getImporte().subtract(getDevoluciones()).subtract(getBonificaciones());
	}
	
	public BigDecimal getUtilidad(){
		return getImporteNeto().subtract(BigDecimal.valueOf(getCosto()));
	}
	
	private BigDecimal totalSegmento=BigDecimal.ZERO;
	private BigDecimal totalUtilidadPorPeriodo=BigDecimal.ZERO;
	
	public BigDecimal getTotalSegmento() {
		return totalSegmento;
	}
	public void setTotalSegmento(BigDecimal totalSegmento) {
		this.totalSegmento = totalSegmento;
	}
	
	
	public BigDecimal getTotalUtilidadPorPeriodo() {
		return totalUtilidadPorPeriodo;
	}

	public void setTotalUtilidadPorPeriodo(BigDecimal totalUtilidadPorPeriodo) {
		this.totalUtilidadPorPeriodo = totalUtilidadPorPeriodo;
	}

	public double getParticipacion(){
		if(getTotalSegmento().doubleValue()<=0)
			return 0;
		return (getImporteNeto().doubleValue()/getTotalSegmento().doubleValue())*100;
	}
	
	public double getParticipacionUtilidad(){
		if(getTotalSegmento().doubleValue()<=0)
			return 0;
		return (getUtilidad().doubleValue()/getTotalUtilidadPorPeriodo().doubleValue())*100;
	}
	
	public double getUtitlidadPorcentual(){
		double util=getUtilidad().doubleValue();
		double net=getImporteNeto().doubleValue();
		if(net<=0)
			return 0;
		return (util/net)*100;
	}


}
