package com.luxsoft.siipap.tesoreria;

import org.springframework.util.StringUtils;

public enum Reportes {
	
	DiarioDeCobranza,
	RecepcionDeFacturas
	;
	
	
	
	public String toString(){
		return StringUtils.uncapitalize(name());
	}

}
