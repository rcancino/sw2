package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UtilidadEnProporcion extends AVNContablePorConcepto{
	
	
	
	private AVNVentaNeta ventaNeta;
	private AVNContablePorConcepto utilidad;

	public UtilidadEnProporcion(String concepto) {
		super(concepto);
	}


	public BigDecimal getImportePorMes(final int mes){
		BigDecimal ventaNeta=getVentaNeta().getImportePorMes(mes);
		BigDecimal costo=getUtilidad().getImportePorMes(mes);
		if(ventaNeta.doubleValue()!=0){
			double res=costo.doubleValue()/ventaNeta.doubleValue();
			return BigDecimal.valueOf(Math.abs(res)*100).setScale(2,RoundingMode.HALF_EVEN);
		}
		return ventaNeta;
		
	}


	public AVNVentaNeta getVentaNeta() {
		return ventaNeta;
	}

	public void setVentaNeta(AVNVentaNeta ventaNeta) {
		this.ventaNeta = ventaNeta;
	}


	public AVNContablePorConcepto getUtilidad() {
		return utilidad;
	}


	public void setUtilidad(AVNContablePorConcepto costos) {
		this.utilidad = costos;
	}
	
	
	

}
