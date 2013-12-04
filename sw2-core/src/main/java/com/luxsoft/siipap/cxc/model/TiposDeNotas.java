package com.luxsoft.siipap.cxc.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public enum TiposDeNotas {
	
	C("Bonificacion Camioneta"),
	Y("Bonificación Jurídico"),
	F("Bonificación Mostrador"),
	L("Nota de Crédito por bonificación"),
	H("Devolución Mostrador"),
	I("Devolución Camioneta"),
	J("Devolución Crédito"),
	T("Descuento Crédito Dic"),
	U("Descuento Crédito 1"),
	V("Descuento dos"),
	W("Descuento adicional"),
	M("Nota de Cargo Cred"),
	Q("Nota de Cargo Cam"),
	O("Nota de Cargo Cheq Dev"),
	P("Nota de Cargo Jurídico"),
	Z("Nota de Cargo Choferes")
	;
	
	
	
	private String desc;
	
	private TiposDeNotas( String desc) {		
		this.desc = desc;
	}
	
	
	public String getDesc() {
		return MessageFormat.format(desc+"  ({0})  ", name());
	}
	
	public static List<String> getStringIds(){
		List<String> ids=new ArrayList<String>();
		for(TiposDeNotas t:values()){
			ids.add(t.name());
		}
		return ids;
	}
	
}

