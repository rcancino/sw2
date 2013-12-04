package com.luxsoft.siipap.model.gastos;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Range;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.UserLog;

/**
 * Indice nacional de precios a l consumidor
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SW_IPC")
public class INPC extends BaseBean implements Comparable<INPC>{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="INPC_ID")
	private Long id;
	
	@Column(name="TASA",precision=6,scale=6,nullable=false)
	private double indice;
		
	@Column (name="YEAR", nullable=false)
	private int year;
	
	@Column (name="MES", nullable=false)
	@Range (min=1,max=12)
	private int mes;
	
	@Embedded
	private UserLog log=new UserLog();
	
	public INPC() {
		final Date date=new Date();
		this.year=Periodo.obtenerYear(date);
		this.mes=Periodo.obtenerMes(date)+1;
	}
	
	public INPC(int year, int mes,double indice) {			
		this.year = year;
		this.mes = mes;
		this.indice = indice;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public int getMes() {
		return mes;
	}
	public void setMes(int mes) {
		int old=this.mes;
		this.mes = mes;
		firePropertyChange("mes", old, mes);
	}
	
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		int old=this.year;
		this.year = year;
		firePropertyChange("year", old, year);
	}
	
	public double getIndice() {
		return indice;
	}
	public void setIndice(double indice) {
		Object old=this.indice;
		this.indice = indice;
		firePropertyChange("indice", old, indice);
	}
	
	public UserLog getLog() {
		return log;
	}
	public void setLog(UserLog log) {
		this.log = log;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + mes;
		result = PRIME * result + year;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final INPC other = (INPC) obj;
		if (mes != other.mes)
			return false;
		if (year != other.year)
			return false;
		return true;
	}
	
	private static DateFormat df;
	
	@Transient
	private Date fecha;
	
	private String format(){	
		if(df==null)
			df=new SimpleDateFormat("MMM-yyyy");
		return df.format(toDate());
	}
	
	public Date toDate(){
		//if(fecha==null){
			Calendar c=Calendar.getInstance();
			c.set(Calendar.YEAR, getYear());
			c.set(Calendar.MONTH, getMes()-1);
			c.set(Calendar.DATE, c.getLeastMaximum(Calendar.DATE));
			fecha=c.getTime();
			return fecha;
		//}		return fecha;
	}
	
	public String toString(){
		String pattern="{0}  ({1}) ";
		return MessageFormat.format(pattern, getIndice(),format());
	}

	public int compareTo(INPC o) {
		return toDate().compareTo(o.toDate());
	}
	
	public static void main(String[] args) {
		INPC i=new INPC(2007,12,128.265);
		System.out.println(i);
	}

}
