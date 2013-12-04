package com.luxsoft.siipap.pos.ui;

import org.springframework.util.StringUtils;

public enum Reportes {
	
	DiarioDeCobranza,
	RecepcionDeFacturas
	;
	
	
	
	public String toString(){
		return StringUtils.uncapitalize(name());
	}

}
