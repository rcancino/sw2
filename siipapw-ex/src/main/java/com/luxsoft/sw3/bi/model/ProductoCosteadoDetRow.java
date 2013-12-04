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
public class ProductoCosteadoDetRow implements Serializable{
	private String tipo;
	private String origen_id;
	private String cliente;
	private Integer docto;
	private Date fechad;
	private String origen;
	private String suc;
	private Integer documento;
	private Date fecha;
	private String linea;
	private String clave;
	private String descripcion;
	private Double ventaNeta;
	private Double costo;
	private Double importeUtilidad;
	private Double porcentajeUtilidad;
	private BigDecimal kilos;
	private BigDecimal precio_kilos;

	
	
	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getOrigen_id() {
		return origen_id;
	}

	public void setOrigen_id(String origen_id) {
		this.origen_id = origen_id;
	}

	public Integer getDocto() {
		return docto;
	}

	public void setDocto(Integer docto) {
		this.docto = docto;
	}

	public Date getFechad() {
		return fechad;
	}

	public void setFechad(Date fechad) {
		this.fechad = fechad;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getSuc() {
		return suc;
	}

	public void setSuc(String suc) {
		this.suc = suc;
	}

	public Integer getDocumento() {
		return documento;
	}

	public void setDocumento(Integer documento) {
		this.documento = documento;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getLinea() {
		return linea;
	}

	public void setLinea(String linea) {
		this.linea = linea;
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
	
	public String getCliente() {
		return cliente;
	}

	public void setCliente(String cliente) {
		this.cliente = cliente;
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
		ProductoCosteadoDetRow other = (ProductoCosteadoDetRow) obj;
		return true;
	}

	



}
