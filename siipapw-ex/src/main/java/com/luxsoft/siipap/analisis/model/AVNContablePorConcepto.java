package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Bean para representar la venta neta desde el punto de vista contable
 * 
 * @author Ruben Cancino
 *
 */
public class AVNContablePorConcepto {
	
	private int year;
	private final String concepto;
	
	protected EventList<ImportesMensuales> importes=new BasicEventList<ImportesMensuales>();
	
	public AVNContablePorConcepto(String concepto){
		this.concepto=concepto;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public EventList<ImportesMensuales> getImportes() {
		return importes;
	}	
	
	
	public String getConcepto() {
		return concepto;
	}
	
	public BigDecimal getEne(){
		return getImportePorMes(1);
	}
	public BigDecimal getFeb(){
		return getImportePorMes(2);
	}
	public BigDecimal getMar(){
		return getImportePorMes(3);
	}
	public BigDecimal getAbr(){
		return getImportePorMes(4);
	}
	public BigDecimal getMay(){
		return getImportePorMes(5);
	}
	public BigDecimal getJun(){
		return getImportePorMes(6);
	}
	public BigDecimal getJul(){
		return getImportePorMes(7);
	}
	public BigDecimal getAgo(){
		return getImportePorMes(8);
	}
	public BigDecimal getSep(){
		return getImportePorMes(9);
	}
	public BigDecimal getOct(){
		return getImportePorMes(10);
	}
	public BigDecimal getNov(){
		return getImportePorMes(11);
	}
	public BigDecimal getDic(){
		return getImportePorMes(12);
	}

	public BigDecimal getImportePorMes(final int mes){
		ImportesMensuales res=(ImportesMensuales)CollectionUtils.find(importes, new Predicate(){
			public boolean evaluate(Object object) {
				ImportesMensuales m=(ImportesMensuales)object;
				if(m.getYear()==year){
					return m.getMes()==mes;
				}
				return false;
			}
		});
		return res!=null?res.getTotal():BigDecimal.ZERO;
	}
	
	/*
	public void actualizar(AVNContablePorConcepto target){
		importes.clear();
		importes.addAll(target.getImportes());
	}
	*/

}
