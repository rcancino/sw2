package com.luxsoft.sw3.bi.inventarios;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;



public class CostosPorArticuloRow {
	
	private String clave;
	private String descripcion;
	private String familia;
	private String familiaDesc;
	private BigDecimal kilos=BigDecimal.ZERO;
	private BigDecimal gramos=BigDecimal.ZERO;
	private String unidad="MIL";
	
	private String periodo;
	private BigDecimal costoPromedio=BigDecimal.ZERO;
	private BigDecimal costoUltimo=BigDecimal.ZERO;
	private BigDecimal existencia=BigDecimal.ZERO;
	
	
	
	public CostosPorArticuloRow() {
		
	}

	public CostosPorArticuloRow(String clave, String descripcion) {		
		this.clave = clave.trim();
		this.descripcion = descripcion.trim();
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

	public BigDecimal getKilos() {
		return kilos;
	}

	public void setKilos(BigDecimal kilos) {
		this.kilos = kilos;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public BigDecimal getGramos() {
		return gramos;
	}

	public void setGramos(BigDecimal gramos) {
		this.gramos = gramos;
	}

	public String getFamilia() {
		return familia;
	}

	public void setFamilia(String familia) {
		this.familia = familia;
	}

	public String getFamiliaDesc() {
		return familiaDesc;
	}

	public void setFamiliaDesc(String familiaDesc) {
		this.familiaDesc = familiaDesc;
	}

	public BigDecimal getCostoPromedio() {
		return costoPromedio;
	}

	public void setCostoPromedio(BigDecimal costoPromedio) {
		this.costoPromedio = costoPromedio;
	}

	public BigDecimal getCostoUltimo() {
		return costoUltimo;
	}

	public void setCostoUltimo(BigDecimal costoUltimo) {
		this.costoUltimo = costoUltimo;
	}

	public BigDecimal getExistencia() {
		return existencia;
	}

	public void setExistencia(BigDecimal existencia) {
		this.existencia = existencia;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}
	
	 
		
	@Override
	public boolean equals(Object obj) {
		if(obj==null)return false;
		if(obj==this)return true;
		CostosPorArticuloRow row=(CostosPorArticuloRow)obj;
		return new EqualsBuilder()
		.append(getClave(),row.getClave())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getClave())
		.toHashCode();
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)		
		.append(getClave())
		.append(getDescripcion())
		.append(getPeriodo())
		.toString();
	}

}
