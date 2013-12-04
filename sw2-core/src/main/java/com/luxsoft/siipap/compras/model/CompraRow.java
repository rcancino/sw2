package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import com.luxsoft.siipap.util.DateUtil;

/**
 * Vista reducida de una Compra para la capa 
 * de presentación (GUI) 
 * 
 * @deprecated Ya no se usara, se usara directamente el bean de compras
 * 
 * @author Ruben Cancino
 *
 */
public class CompraRow {
	
	private Long id;
	private String sucursal;
	private String proveedor;
	private BigDecimal total;
	private Currency moneda;
	private Date fecha;
	private String estado;
	
	
	
	
	public CompraRow() {}
	
	public CompraRow(final Compra compra) {
		
		setId(compra.getId());
		setFecha(compra.getFecha());
		setMoneda(compra.getMoneda());
		setProveedor(compra.getProveedor().getNombreRazon());
		setSucursal(compra.getSucursal().getNombre());
		setMoneda(compra.getMoneda());
		setTotal(compra.getTotal().amount());
		
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
		return DateUtil.toYear(getFecha());
	}
	public int getMes(){
		return DateUtil.toMes(getFecha());
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
		final CompraRow other = (CompraRow) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	

}
