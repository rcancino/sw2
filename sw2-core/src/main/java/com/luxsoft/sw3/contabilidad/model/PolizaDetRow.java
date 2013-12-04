package com.luxsoft.sw3.contabilidad.model;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.util.SQLUtils;



public class PolizaDetRow {
	
	private long poliza;
	private long polizaid;
	private String tipo;
	private String cuenta;
	private String concepto;
	private String descripcion;
	private String descripcion2;
	private String referencia;
	private String referencia2;
	private BigDecimal debe;
	private BigDecimal haber;
	private int year;
	private int mes;
	private Date creado;
	
	public long getPoliza() {
		return poliza;
	}

	public void setPoliza(long poliza) {
		this.poliza = poliza;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescripcion2() {
		return descripcion2;
	}

	public void setDescripcion2(String descripcion2) {
		this.descripcion2 = descripcion2;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getReferencia2() {
		return referencia2;
	}

	public void setReferencia2(String referencia2) {
		this.referencia2 = referencia2;
	}

	public BigDecimal getDebe() {
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		this.debe = debe;
	}

	public BigDecimal getHaber() {
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		this.haber = haber;
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

	public Date getCreado() {
		return creado;
	}

	public void setCreado(Date creado) {
		this.creado = creado;
	}
	
	

	public long getPolizaid() {
		return polizaid;
	}

	public void setPolizaid(long polizaid) {
		this.polizaid = polizaid;
	}

	public static void printPropiedadesArray(){
		
	}

	public static void main(String[] args) {
		
		String sql="select * from inf_cont_diod_2012_01";
		//SQLUtils.printBeanClasFromSQL(sql, true);
		SQLUtils.printBeanPropertiesSQL(sql,true);
	}

	
	
	

}

