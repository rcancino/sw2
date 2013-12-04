package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

/**
 * Vista reducida de una Compra para la capa 
 * de presentación (GUI) 
 * 
 * @author Ruben Cancino
 *
 */
public class GCompraRow {
	
	private Long id;
	private String sucursal;
	private String departamento;
	private String proveedor;
	private BigDecimal total;
	private double tc;
	private Currency moneda;
	private Date fecha;
	private String estado;
	private String factura;
	private int year;
	private int mes;
	private String tipo;
	
	
	
	
	public GCompraRow() {}
	
	public GCompraRow(final GCompra compra) {
		setId(compra.getId());
		setDepartamento(compra.getDepartamento().getClave());
		setEstado(compra.getEstado().name());		
		setFecha(compra.getFecha());
		setMes(compra.getMes());
		setYear(compra.getYear());
		setMoneda(compra.getMoneda());
		setProveedor(compra.getProveedor().getNombreRazon());
		setSucursal(compra.getSucursal().getNombre());
		setTotal(compra.getTotal());
		if(!compra.getFacturas().isEmpty()){
			setFactura(compra.getFacturas().iterator().next().getDocumento());
		}
		setTc(compra.getTc().doubleValue());
		setTipo(compra.getTipo().name());
	}
	
	public String getDepartamento() {
		return departamento;
	}
	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getMes() {
		return mes;
	}
	public void setMes(int mes) {
		this.mes = mes;
	}
	public Currency getMoneda() {
		return moneda;
	}
	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}
	public String getProveedor() {
		return proveedor;
	}
	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}
	
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
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
		final GCompraRow other = (GCompraRow) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * @return the factura
	 */
	public String getFactura() {
		return factura;
	}

	/**
	 * @param factura the factura to set
	 */
	public void setFactura(String factura) {
		this.factura = factura;
	}

	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		this.tc = tc;
	}
	
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

}
