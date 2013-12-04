/**
 * 
 */
package com.luxsoft.siipap.inventario.ui.consultas;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MovimientoCosteadoRow{
	
	private String clave;
	private String descripcion;
	private String linea;
	private String clase;
	private String marca;
	private BigDecimal saldoInicial;
	private BigDecimal costoIni;
	private BigDecimal comsSinAUni;
	private BigDecimal comsSinA;
	private BigDecimal comsUni;
	private BigDecimal comsCosto;
	private BigDecimal maqUni;
	private BigDecimal maqCosto;
	private BigDecimal servicioCosto;
	private BigDecimal movsUni;
	private BigDecimal movsCosto;
	private BigDecimal decUni;
	private BigDecimal decCosto;
	private BigDecimal trasladosUni;
	private BigDecimal trasladosCosto;
	private BigDecimal trsSalUni;
	private BigDecimal trsSalCosto;
	private BigDecimal trsEntUni;
	private BigDecimal trsEntCosto;
	private BigDecimal devUni;
	private BigDecimal devCosto;
	private BigDecimal vtaUni;
	private BigDecimal vtaCosto;
	private BigDecimal SALDO;
	private BigDecimal COSTO;
	private BigDecimal COSTOP;
	
	
	
	
	
	
	
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
	public BigDecimal getSaldoInicial() {
		return saldoInicial;
	}
	public void setSaldoInicial(BigDecimal saldoInicial) {
		this.saldoInicial = saldoInicial;
	}
	public BigDecimal getCostoIni() {
		return costoIni;
	}
	public void setCostoIni(BigDecimal costoIni) {
		this.costoIni = costoIni;
	}
	public BigDecimal getComsSinA() {
		return comsSinA;
	}
	public void setComsSinA(BigDecimal comsSinA) {
		this.comsSinA = comsSinA;
	}
	public BigDecimal getComsUni() {
		return comsUni;
	}
	public void setComsUni(BigDecimal comsUni) {
		this.comsUni = comsUni;
	}
	public BigDecimal getComsCosto() {
		return comsCosto;
	}
	public void setComsCosto(BigDecimal comsCosto) {
		this.comsCosto = comsCosto;
	}
	public BigDecimal getMaqUni() {
		return maqUni;
	}
	public void setMaqUni(BigDecimal maqUni) {
		this.maqUni = maqUni;
	}
	public BigDecimal getMaqCosto() {
		return maqCosto;
	}
	public void setMaqCosto(BigDecimal maqCosto) {
		this.maqCosto = maqCosto;
	}
	public BigDecimal getServicioCosto() {
		return servicioCosto;
	}
	public void setServicioCosto(BigDecimal servicioCosto) {
		this.servicioCosto = servicioCosto;
	}
	public BigDecimal getMovsCosto() {
		return movsCosto;
	}
	public void setMovsCosto(BigDecimal movsCosto) {
		this.movsCosto = movsCosto;
	}
	public BigDecimal getTrasladosCosto() {
		return trasladosCosto;
	}
	public void setTrasladosCosto(BigDecimal trasladosCosto) {
		this.trasladosCosto = trasladosCosto;
	}
	public BigDecimal getTrsSalCosto() {
		return trsSalCosto;
	}
	public void setTrsSalCosto(BigDecimal trsSalCosto) {
		this.trsSalCosto = trsSalCosto;
	}
	public BigDecimal getTrsEntUni() {
		return trsEntUni;
	}
	public void setTrsEntUni(BigDecimal trsEntUni) {
		this.trsEntUni = trsEntUni;
	}
	public BigDecimal getTrsEntCosto() {
		return trsEntCosto;
	}
	public void setTrsEntCosto(BigDecimal trsEntCosto) {
		this.trsEntCosto = trsEntCosto;
	}
	public BigDecimal getDevCosto() {
		return devCosto;
	}
	public void setDevCosto(BigDecimal devCosto) {
		this.devCosto = devCosto;
	}
	public BigDecimal getVtaCosto() {
		return vtaCosto;
	}
	public void setVtaCosto(BigDecimal vtaCosto) {
		this.vtaCosto = vtaCosto;
	}
	public BigDecimal getSALDO() {
		return SALDO;
	}
	public void setSALDO(BigDecimal saldo) {
		SALDO = saldo;
	}
	public BigDecimal getCOSTO() {
		return COSTO;
	}
	public void setCOSTO(BigDecimal costo) {
		COSTO = costo;
	}
	public BigDecimal getCOSTOP() {
		return COSTOP;
	}
	public void setCOSTOP(BigDecimal costop) {
		COSTOP = costop;
	}
	
	
	
	public BigDecimal getDecCosto() {
		return decCosto;
	}
	public void setDecCosto(BigDecimal decCosto) {
		this.decCosto = decCosto;
	}
	
/*	public BigDecimal getCostoAbs(){
		return getComsCosto().abs()
		.add(getComsSinA().abs())
		.add(getCostoIni().abs())
		.add(getDevCosto().abs())
		.add(getMaqCosto().abs())
		.add(getMovsCosto().abs())
		.add(getServicioCosto().abs())
		.add(getTrasladosCosto().abs())
		.add(getTrsEntCosto().abs())
		.add(getTrsSalCosto().abs())
		.add(getVtaCosto().abs())
		.add(getDecCosto().abs())
		.abs();
	
	}
	public boolean isVisible(){
		return getCostoAbs().doubleValue()>0;
	} */
	

	public BigDecimal getCostoAbs(){
		return getComsUni().abs()
		.add(getComsSinAUni().abs())
		.add(getSaldoInicial().abs())
		.add(getDevUni().abs())
		.add(getMaqUni().abs())
		.add(getMovsUni().abs())
	//	.add(getServicioCosto().abs())
		.add(getTrasladosUni().abs())
		.add(getTrsEntUni().abs())
		.add(getTrsSalUni().abs())
		.add(getVtaUni().abs())
		.add(getDecUni().abs())
		.abs();
	
	}
	
	public boolean isVisible(){
		return getCostoAbs().doubleValue()>0;
	}
	
	/**
	 * 
	
	private BigDecimal costoIni;
	private BigDecimal comsSinA;
	private BigDecimal comsCosto;
	private BigDecimal maqCosto;
	private BigDecimal servicioCosto;
	private BigDecimal movsCosto;
	private BigDecimal trasladosCosto;
	private BigDecimal trsSalCosto;	
	private BigDecimal trsEntCosto;
	private BigDecimal devCosto;
	private BigDecimal vtaCosto;
	
	
	 * 
	 */
	public BigDecimal getCostoFin(){
		return getCostoIni()
				.add(getComsSinA()
				.add(getComsCosto())
				.add(getMaqCosto())
				.add(getServicioCosto())
				.add(getMovsCosto())
				.add(getTrasladosCosto())
				.add(getTrsSalCosto())
				.add(getTrsEntCosto())
				.add(getDevCosto())
				.add(getDecCosto())
				.add(getVtaCosto())
				);
		
	}
	
	public BigDecimal getDif(){
		return getCOSTO().subtract(getCostoFin());
	}
	
	public BigDecimal getUniFin(){
		return getSaldoInicial()
				.add(getComsSinAUni()
				.add(getComsUni())
				.add(getMaqUni())
				//.add(getServicioCosto())
				.add(getMovsUni())
				.add(getTrasladosUni())
				.add(getTrsSalUni())
				.add(getTrsEntUni())
				.add(getDevUni())
				.add(getDecUni())
				.add(getVtaUni())
				);
		
	}
	
	public BigDecimal getDifUni(){
		return getSALDO().subtract(getUniFin());
	}
	
	public BigDecimal getCalCostoParaPromedio(){
		if(getSaldoInicial().doubleValue()>0)
			return getCostoIni()
				.add(getComsCosto()
				.add(getMaqCosto())
				.add(getServicioCosto())
				.add(getTrsEntCosto())
				);
		else{
			return
					getComsCosto()
					.add(getMaqCosto())
					.add(getServicioCosto())
					.add(getTrsEntCosto())
					;
		}
		
	}
	
	public BigDecimal getCalUniParaPromedio(){
		BigDecimal cantidad=BigDecimal.ZERO;
		cantidad=cantidad
				.add(getComsUni()
				.add(getMaqUni())
				.add(getTrsEntUni())
						);
		if(getSaldoInicial().doubleValue()>0)
			cantidad=cantidad.add(getSaldoInicial());
		return cantidad;

	}

	public BigDecimal getCalCostoP(){
		if(getCalUniParaPromedio().doubleValue()<=0)
			return getCalCostoParaPromedio();
		return getCalCostoParaPromedio().divide(getCalUniParaPromedio(),RoundingMode.HALF_EVEN);
	}
	
	public BigDecimal getDifCostoP(){
		return getCOSTOP().subtract(getCalCostoP());
	}
	
	public BigDecimal getComsSinAUni() {
		return comsSinAUni;
	}
	public void setComsSinAUni(BigDecimal comsSinAUni) {
		this.comsSinAUni = comsSinAUni;
	}
	public BigDecimal getMovsUni() {
		return movsUni;
	}
	public void setMovsUni(BigDecimal movsUni) {
		this.movsUni = movsUni;
	}
	public BigDecimal getDecUni() {
		return decUni;
	}
	public void setDecUni(BigDecimal decUni) {
		this.decUni = decUni;
	}
	public BigDecimal getTrasladosUni() {
		return trasladosUni;
	}
	public void setTrasladosUni(BigDecimal trasladosUni) {
		this.trasladosUni = trasladosUni;
	}
	public BigDecimal getTrsSalUni() {
		return trsSalUni;
	}
	public void setTrsSalUni(BigDecimal trsSalUni) {
		this.trsSalUni = trsSalUni;
	}
	public BigDecimal getDevUni() {
		return devUni;
	}
	public void setDevUni(BigDecimal devUni) {
		this.devUni = devUni;
	}
	public BigDecimal getVtaUni() {
		return vtaUni;
	}
	public void setVtaUni(BigDecimal vtaUni) {
		this.vtaUni = vtaUni;
	}
	
}