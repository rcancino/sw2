package com.luxsoft.sw3.contabilidad.services;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Bean cobranza generica de las operaciones
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class CCobranza {
	
	private String tipo;
	private String origen_id;
	private Date fecha;
	private String sucursal;
	private String abono_id;
	private String origen;
	private String concepto;
	private BigDecimal importe;
	private String banco;
	private String descripcion;
	private String nombre;
	
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
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	public String getAbono_id() {
		return abono_id;
	}
	public void setAbono_id(String abono_id) {
		this.abono_id = abono_id;
	}
	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	public String getConcepto() {
		return concepto;
	}
	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}
	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}
	public String getBanco() {
		return banco;
	}
	public void setBanco(String banco) {
		this.banco = banco;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((origen_id == null) ? 0 : origen_id.hashCode());
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
		CCobranza other = (CCobranza) obj;
		if (origen_id == null) {
			if (other.origen_id != null)
				return false;
		} else if (!origen_id.equals(other.origen_id))
			return false;
		return true;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	

	
	

}
