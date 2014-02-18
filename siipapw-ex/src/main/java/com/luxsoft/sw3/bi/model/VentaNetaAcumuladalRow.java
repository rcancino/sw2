package com.luxsoft.sw3.bi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.cxc.ui.selectores.DevolucionRow;
import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Una version limitada del cargo de un cliente
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VentaNetaAcumuladalRow implements Serializable{

	private String periodo;
	private String origenId;
	private String descripcion;
	private Double ventaNeta;
	private Double costo;
	private Double importeUtilidad;
	private Double porcentajeUtilidad;
	private BigDecimal porcentajeAportacion;
	private Double inventarioCosteado;
	private String nacional;
	private BigDecimal kilos;
	private BigDecimal precio_kilos;
	private BigDecimal porcentajePartVN;
	
	
	
	
	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Double getVentaNeta() {
		return ventaNeta;
	}

	public void setVentaNeta(Double ventaNeta) {
		this.ventaNeta = ventaNeta;
	}

	public Double getCosto() {
		return costo;
	}

	public void setCosto(Double costo) {
		this.costo = costo;
	}

	public Double getImporteUtilidad() {
		return importeUtilidad;
	}

	public void setImporteUtilidad(Double importeUtilidad) {
		this.importeUtilidad = importeUtilidad;
	}

	public Double getPorcentajeUtilidad() {
		return porcentajeUtilidad;
	}

	public void setPorcentajeUtilidad(Double porcentajeUtilidad) {
		this.porcentajeUtilidad = porcentajeUtilidad;
	}

	public BigDecimal getPorcentajeAportacion() {
		return porcentajeAportacion;
	}

	public void setPorcentajeAportacion(BigDecimal porcentajeAp) {
		this.porcentajeAportacion = porcentajeAp;
	}

	public Double getInventarioCosteado() {
		return inventarioCosteado;
	}

	public void setInventarioCosteado(Double inventarioCosteado) {
		this.inventarioCosteado = inventarioCosteado;
	}
	public void setKilos(BigDecimal kilos) {
		this.kilos = kilos;
	}
	public BigDecimal getKilos() {
		return kilos;
	}
	

	public BigDecimal getPrecio_kilos() {
		return precio_kilos;
	}

	public void setPrecio_kilos(BigDecimal precio_kilos) {
		this.precio_kilos = precio_kilos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descripcion == null) ? 0 : descripcion.hashCode());
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
		VentaNetaAcumuladalRow other = (VentaNetaAcumuladalRow) obj;
		return true;
	}

	public String getNacional() {
		return nacional;
	}

	public void setNacional(String nacional) {
		this.nacional = nacional;
	}

	public String getOrigenId() {
		return origenId;
	}

	public void setOrigenId(String origenId) {
		this.origenId = origenId;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public BigDecimal getPorcentajePartVN() {
		return porcentajePartVN;
	}

	public void setPorcentajePartVN(BigDecimal porcentajePartVN) {
		this.porcentajePartVN = porcentajePartVN;
	}


}
