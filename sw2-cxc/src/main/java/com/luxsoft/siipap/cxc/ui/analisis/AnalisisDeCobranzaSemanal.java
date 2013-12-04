package com.luxsoft.siipap.cxc.ui.analisis;

import java.math.BigDecimal;

import com.luxsoft.siipap.util.DateUtil;


/**
 * Representa importes de las 5 semanas de un mes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeCobranzaSemanal {
	
	private int year;
	
	private int mesId;
	
	private ImporteSemanal cobranza=new ImporteSemanal();
	
	private ImporteSemanal ventas=new ImporteSemanal();
	
	private ImporteSemanal cartera;
	
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getMesId() {
		return mesId;
	}
	public void setMesId(int mes) {
		this.mesId = mes;
	}

	public ImporteSemanal getCobranza() {
		return cobranza;
	}

	public void setCobranza(ImporteSemanal cobranza) {
		this.cobranza = cobranza;
	}

	public ImporteSemanal getVentas() {
		return ventas;
	}

	public void setVentas(ImporteSemanal ventas) {
		this.ventas = ventas;
	}

	public ImporteSemanal getCartera() {
		return cartera;
	}

	public void setCartera(ImporteSemanal cartera) {
		this.cartera = cartera;
	}
	
	public String getMes(){
		return DateUtil.getMesAsString(getMesId());
	}
	
	public BigDecimal getVentas1(){
		return ventas.getSemana1();
	}
	
	public BigDecimal getVentas2(){
		return ventas.getSemana2();
	}
	
	public BigDecimal getVentas3(){
		return ventas.getSemana3();
	}
	
	public BigDecimal getVentas4(){
		return ventas.getSemana4();
	}
	
	public BigDecimal getVentas5(){
		return ventas.getSemana5();
	}
	
	public BigDecimal getCobranza1(){
		return cobranza.getSemana1();
	}
	
	public BigDecimal getCobranza2(){
		return cobranza.getSemana2();
	}
	
	public BigDecimal getCobranza3(){
		return cobranza.getSemana3();
	}
	
	public BigDecimal getCobranza4(){
		return cobranza.getSemana4();
	}
	
	public BigDecimal getCobranza5(){
		return cobranza.getSemana5();
	}

}
