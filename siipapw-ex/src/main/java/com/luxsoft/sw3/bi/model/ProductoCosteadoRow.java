package com.luxsoft.sw3.bi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import junitx.util.PrivateAccessor;

import com.luxsoft.siipap.cxc.ui.selectores.DevolucionRow;
import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Una version limitada del cargo de un cliente
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ProductoCosteadoRow implements Serializable{
	
	private String linea;
	private String clase;
	private String marca;
	private String clave;
	private String descripcion;
	private Double kilosMillar;
	private Integer gramos;
	private Integer calibre;
	private Integer caras;
	private String deLinea;
	private String nacional;
	private Double ventaNeta;
	private Double costo;
	private Double importeUtilidad;
	private Double porcentajeUtilidad;
	private BigDecimal kilos;
	private BigDecimal precio_kilos;
	private BigDecimal costo_kilos;

	
	public String getLinea() {
		return linea;
	}

	public void setLinea(String linea) {
		this.linea = linea;
	}
	
	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
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

	public Double getKilosMillar() {
		return kilosMillar;
	}

	public void setKilosMillar(Double kilosMillar) {
		this.kilosMillar = kilosMillar;
	}

	public Integer getGramos() {
		return gramos;
	}

	public void setGramos(Integer gramos) {
		this.gramos = gramos;
	}

	public Integer getCalibre() {
		return calibre;
	}

	public void setCalibre(Integer calibre) {
		this.calibre = calibre;
	}

	public Integer getCaras() {
		return caras;
	}

	public void setCaras(Integer caras) {
		this.caras = caras;
	}

	public String getDeLinea() {
		return deLinea;
	}

	public void setDeLinea(String deLinea) {
		this.deLinea = deLinea;
	}

	public String getNacional() {
		return nacional;
	}

	public void setNacional(String nacional) {
		this.nacional = nacional;
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

	
	
	public BigDecimal getKilos() {
		return kilos;
	}

	public void setKilos(BigDecimal kilos) {
		this.kilos = kilos;
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
		ProductoCosteadoRow other = (ProductoCosteadoRow) obj;
		return true;
	}

	public BigDecimal getCosto_kilos() {
		return costo_kilos;
	}

	public void setCosto_kilos(BigDecimal costo_kilos) {
		this.costo_kilos = costo_kilos;
	}



	



}
