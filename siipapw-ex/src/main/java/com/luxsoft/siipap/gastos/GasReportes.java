package com.luxsoft.siipap.gastos;

import org.springframework.util.StringUtils;

public enum GasReportes {
	
	DiarioDeCobranza,
	RecepcionDeFacturas
	;
	
	
	
	public String toString(){
		return StringUtils.uncapitalize(name());
	}

}
