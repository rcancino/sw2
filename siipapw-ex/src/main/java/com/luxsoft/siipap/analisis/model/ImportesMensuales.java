package com.luxsoft.siipap.analisis.model;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.StringUtils;

/**
 * Bean que representa importes de un año mes fragmentado en por periodos
 * mensuales. Este termino complejo y abstracto se puede utiliza para
 * representar como se fragmentan las notas de credito con respecto a
 * la venta a la que se refieren
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class ImportesMensuales {
	
	
	
	private int year;
	private int mes;
	private BigDecimal ene=BigDecimal.ZERO;
	private BigDecimal feb=BigDecimal.ZERO;
	private BigDecimal mar=BigDecimal.ZERO;
	private BigDecimal abr=BigDecimal.ZERO;
	private BigDecimal may=BigDecimal.ZERO;
	private BigDecimal jun=BigDecimal.ZERO;
	private BigDecimal jul=BigDecimal.ZERO;
	private BigDecimal ago=BigDecimal.ZERO;
	private BigDecimal sep=BigDecimal.ZERO;
	private BigDecimal oct=BigDecimal.ZERO;
	private BigDecimal nov=BigDecimal.ZERO;
	private BigDecimal dic=BigDecimal.ZERO;
	private BigDecimal total=BigDecimal.ZERO;
	
	public ImportesMensuales(){
		
	}

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

	public BigDecimal getEne() {
		return ene;
	}

	public void setEne(BigDecimal ene) {
		this.ene = ene;
	}

	public BigDecimal getFeb() {
		return feb;
	}

	public void setFeb(BigDecimal feb) {
		this.feb = feb;
	}

	public BigDecimal getMar() {
		return mar;
	}

	public void setMar(BigDecimal mar) {
		this.mar = mar;
	}

	public BigDecimal getAbr() {
		return abr;
	}

	public void setAbr(BigDecimal abr) {
		this.abr = abr;
	}

	public BigDecimal getMay() {
		return may;
	}

	public void setMay(BigDecimal may) {
		this.may = may;
	}

	public BigDecimal getJun() {
		return jun;
	}

	public void setJun(BigDecimal jun) {
		this.jun = jun;
	}

	public BigDecimal getJul() {
		return jul;
	}

	public void setJul(BigDecimal jul) {
		this.jul = jul;
	}

	public BigDecimal getAgo() {
		return ago;
	}

	public void setAgo(BigDecimal ago) {
		this.ago = ago;
	}

	public BigDecimal getSep() {
		return sep;
	}

	public void setSep(BigDecimal sep) {
		this.sep = sep;
	}

	public BigDecimal getOct() {
		return oct;
	}

	public void setOct(BigDecimal oct) {
		this.oct = oct;
	}

	public BigDecimal getNov() {
		return nov;
	}

	public void setNov(BigDecimal nov) {
		this.nov = nov;
	}

	public BigDecimal getDic() {
		return dic;
	}

	public void setDic(BigDecimal dic) {
		this.dic = dic;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	/**
	 * 
	 * @param valor
	 * @param mes
	 */
	public void setImportePorMes(final BigDecimal valor,int mes){
		String month=AnalisisUtils.MESES[--mes];
		month=StringUtils.uncapitalize(month);
		try {
			BeanUtils.setProperty(this, month, valor);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(year)
		.append(mes)
		.append(total)
		.toString()
		;
	}

}
