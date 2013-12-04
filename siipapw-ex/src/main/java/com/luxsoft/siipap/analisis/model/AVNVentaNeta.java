package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

public class AVNVentaNeta extends AVNContablePorConcepto{
	
	
	public AVNVentaNeta(String concepto) {
		super(concepto);
	}

	public AVNVentaNeta() {
		super("Venta Neta");
	}
	
	public BigDecimal getImportePorMes(final int mes){
		Collection<ImportesMensuales> res=CollectionUtils.select(importes,new Predicate(){
			public boolean evaluate(Object object) {
				ImportesMensuales m=(ImportesMensuales)object;
				if(m.getYear()==getYear()){
					return m.getMes()==mes;
				}
				return false;
			}
		});
		BigDecimal t=BigDecimal.ZERO;
		for(ImportesMensuales c:res){
			t=t.add(c.getTotal());
		}
		return t;
		
	}

}
