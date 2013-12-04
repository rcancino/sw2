package com.luxsoft.siipap.cxc.ui.analisis;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Representa importes de las 5 semanas de un mes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImporteSemanal {
	
	private int year;
	private int mesId;
	private Map<Integer, Number> importeSemanal=new HashMap<Integer, Number>();
	
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
	
	public Map<Integer, Number> getImporteSemanal() {
		return importeSemanal;
	}
	
	
	public String getMes(){
		return DateUtil.getMesAsString(getMesId());
	}
	
	protected BigDecimal getImporte(int semana){
		Number val=importeSemanal.get(1);
		CantidadMonetaria valor=CantidadMonetaria.pesos(0);
		if(val!=null)
			valor=CantidadMonetaria.pesos(val.doubleValue());
		return valor.amount();
	}
	
	public BigDecimal getSemana1(){
		return getImporte(1);
	}
	
	public BigDecimal getSemana2(){
		return getImporte(1);
	}
	
	public BigDecimal getSemana3(){
		return getImporte(3);
	}
	
	public BigDecimal getSemana4(){
		return getImporte(4);
	}
	
	public BigDecimal getSemana5(){
		return getImporte(5);
	}

}
