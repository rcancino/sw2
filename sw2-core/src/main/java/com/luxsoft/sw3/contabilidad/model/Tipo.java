package com.luxsoft.sw3.contabilidad.model;

import java.util.Arrays;
import java.util.List;

public enum Tipo {
	
	ACTIVO("CIRCULANTE","FIJO","DIFERIDO")
	,PASIVO("CORTO PLAZO","LARGO PLAZO")
	,CAPITAL("CAPITAL")
	,ORDEN("ORDEN")
	;
	
	private final String[] subTipos;
	
	private Tipo(String...subTipos){
		this.subTipos=subTipos;
	}
	
	public String[] getSubTipos(){
		return subTipos;
	}
	
	public List<String> getSubTiposList(){
		return Arrays.asList(getSubTipos());
	}

}
