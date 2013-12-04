package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.model.Periodo;

/**
 * Elemento de inventario q contribuye al costo promedio del inventario
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CostoPromedioItem {
	
	private Date fecha;
	private int sucursal;
	private long documento;
	private String sucursalName;
	private String clave;
	private String descripcion;
	private String unidad;
	private double cantidad;
	private BigDecimal costo;
	private String tipo;
	
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
	public String getUnidad() {
		return unidad;
	}
	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}
	public double getCantidad() {
		return cantidad;
	}
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}
	public BigDecimal getCosto() {
		return costo;
	}
	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}
	public int getSucursal() {
		return sucursal;
	}
	public void setSucursal(int sucursal) {
		this.sucursal = sucursal;
	}
	public String getSucursalName() {
		return sucursalName;
	}
	public void setSucursalName(String sucursalName) {
		this.sucursalName = sucursalName;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public long getDocumento() {
		return documento;
	}
	public void setDocumento(long documento) {
		this.documento = documento;
	}
	
	public int getMes(){
		return Periodo.obtenerMes(fecha);
	}
	
	public int getYear(){
		return Periodo.obtenerYear(fecha);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clave == null) ? 0 : clave.hashCode());
		result = prime * result + (int) (documento ^ (documento >>> 32));
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + sucursal;
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
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
		CostoPromedioItem other = (CostoPromedioItem) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		if (documento != other.documento)
			return false;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (sucursal != other.sucursal)
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}
	
	

}
