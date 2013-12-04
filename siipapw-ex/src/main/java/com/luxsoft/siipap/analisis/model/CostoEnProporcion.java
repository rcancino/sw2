package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CostoEnProporcion extends AVNContablePorConcepto{
	
	
	
	private AVNVentaNeta ventaNeta;
	private AVNContablePorConcepto costos;

	public CostoEnProporcion(String concepto) {
		super(concepto);
	}


	public BigDecimal getImportePorMes(final int mes){
		BigDecimal ventaNeta=getVentaNeta().getImportePorMes(mes);
		BigDecimal costo=getCostos().getImportePorMes(mes);
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


	public AVNContablePorConcepto getCostos() {
		return costos;
	}


	public void setCostos(AVNContablePorConcepto costos) {
		this.costos = costos;
	}
	
	
	

}
