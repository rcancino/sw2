package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.math.BigDecimal;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;

public class VentaPorSucursal {

	private String clave;
	private String sucursal;
	private OrigenDeOperacion origen;
	private BigDecimal importe;
	private BigDecimal impuesto;
	private BigDecimal total;
	private BigDecimal anticipoAplicado;
	private Boolean anticipo;
	
	public VentaPorSucursal() {
		
	}

	public VentaPorSucursal(String sucursal, OrigenDeOperacion origen, BigDecimal importe,
			BigDecimal impuesto,
			BigDecimal total,
			BigDecimal anticipoAplicado,
			String clave,
			Boolean anticipo) {		
		this.sucursal = sucursal;
		this.origen = origen;
		this.importe = importe;
		this.impuesto = impuesto;
		this.total=total;
		this.anticipoAplicado=anticipoAplicado;
		this.clave=clave;
		this.anticipo= anticipo;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
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
	
	public Boolean getAnticipo() {
		return anticipo;
	}

	public BigDecimal getAnticipoAplicado() {
		return anticipoAplicado;
	}

	public void setAnticipoAplicado(BigDecimal anticipoAplicado) {
		this.anticipoAplicado = anticipoAplicado;
	}

	@Override
	public String toString() {
		return "VentaPorSucursal [clave=" + clave + ", sucursal=" + sucursal
				+ ", origen=" + origen + ", importe=" + importe + ", impuesto="
				+ impuesto + ", total=" + total + 
				", anticipoAplicado=" + anticipoAplicado + 
				", anticipo=" + anticipo + "]";
	}


}
