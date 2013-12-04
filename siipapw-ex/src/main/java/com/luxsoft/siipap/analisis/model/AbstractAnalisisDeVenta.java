package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jdesktop.beans.AbstractBean;

public class AbstractAnalisisDeVenta {
	
	private int year;
	private int mes;
	private String linea;
	
	private BigDecimal toneladasCre=BigDecimal.ZERO;
	private BigDecimal toneladasCon=BigDecimal.ZERO;
	
	private BigDecimal ventaNetaCon=BigDecimal.ZERO;
	private BigDecimal ventaNetaCre=BigDecimal.ZERO;
	
	private BigDecimal ventaBrutaCon=BigDecimal.ZERO;
	private BigDecimal ventaBrutaCre=BigDecimal.ZERO;
	
	private BigDecimal costoCre=BigDecimal.ZERO;
	private BigDecimal costoCon=BigDecimal.ZERO;
	
	private List<AbstractAnalisisDeVenta> children=new ArrayList<AbstractAnalisisDeVenta>();
	
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMes() {
		return mes;
	}
	public void setMes(int mes) {
		this.mes = mes;
	}
	public String getLinea() {
		return linea;
	}
	public void setLinea(String linea) {
		this.linea = linea;
	}
	public BigDecimal getToneladasCre() {
		return toneladasCre;
	}
	public void setToneladasCre(BigDecimal toneladasCre) {
		this.toneladasCre = toneladasCre;
	}
	public BigDecimal getToneladasCon() {
		return toneladasCon;
	}
	public void setToneladasCon(BigDecimal toneladasCon) {
		this.toneladasCon = toneladasCon;
	}
	public BigDecimal getVentaNetaCon() {
		return ventaNetaCon;
	}
	public void setVentaNetaCon(BigDecimal ventaNetaCon) {
		this.ventaNetaCon = ventaNetaCon;
	}
	public BigDecimal getVentaNetaCre() {
		return ventaNetaCre;
	}
	public void setVentaNetaCre(BigDecimal ventaNetaCre) {
		this.ventaNetaCre = ventaNetaCre;
	}
	public BigDecimal getVentaBrutaCon() {
		return ventaBrutaCon;
	}
	public void setVentaBrutaCon(BigDecimal ventaBrutaCon) {
		this.ventaBrutaCon = ventaBrutaCon;
	}
	public BigDecimal getVentaBrutaCre() {
		return ventaBrutaCre;
	}
	public void setVentaBrutaCre(BigDecimal ventaBrutaCre) {
		this.ventaBrutaCre = ventaBrutaCre;
	}
	public BigDecimal getCostoCre() {
		return costoCre;
	}
	public void setCostoCre(BigDecimal costoCre) {
		this.costoCre = costoCre;
	}
	public BigDecimal getCostoCon() {
		return costoCon;
	}
	public void setCostoCon(BigDecimal costoCon) {
		this.costoCon = costoCon;
	}
	
	public List<AbstractAnalisisDeVenta> getChilren(){
		return children;
	}
	
	public void consolidar(){
		toneladasCre=BigDecimal.ZERO;
		/*
		toneladasCon=BigDecimal.ZERO;
		
		ventaNetaCon=BigDecimal.ZERO;
		ventaNetaCre=BigDecimal.ZERO;
		
		ventaBrutaCon=BigDecimal.ZERO;
		ventaBrutaCre=BigDecimal.ZERO;
		
		costoCre=BigDecimal.ZERO;
		costoCon=BigDecimal.ZERO;
		*/
		for(AbstractAnalisisDeVenta a:children){			
			toneladasCre=toneladasCre.add(a.getToneladasCre());
		}
	}
	
}
