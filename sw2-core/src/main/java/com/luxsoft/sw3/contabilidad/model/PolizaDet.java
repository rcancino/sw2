package com.luxsoft.sw3.contabilidad.model;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;


@Entity
@Table (name="SX_POLIZASDET")
public class PolizaDet extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="POLIZADET_ID")
	private Long id;
	
	@Version
    private int version;
	
	@ManyToOne(optional = false)
    @JoinColumn(name = "POLIZA_ID"
    	, nullable = false
    	, updatable = false,insertable=false
    	)
	private Poliza poliza;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="CUENTA_ID", nullable=false) 
    @NotNull
	private CuentaContable cuenta;
	
	@ManyToOne (optional=true)
    @JoinColumn (name="CONCEPTO_ID", nullable=true) 
    //@NotNull
	private ConceptoContable concepto;
	
	
	@Column(name="DESCRIPCION",nullable=false)
	@NotNull
	@Length(max=255)
	private String descripcion;
	
	@Column(name="DESCRIPCION2",nullable=true)
	@Length(max=255)
	private String descripcion2="";
	
	@Column(name="ASIENTO")
	@Length(max=255)
	private String asiento;
	
	@Column(name="REFERENCIA")
	@Length(max=255)
	private String referencia;
	
	@Column(name="REFERENCIA2")
	@Length(max=255)
	private String referencia2;
	
	@Column (name="DEBE",nullable=false,scale=6,precision=16)
	private BigDecimal debe=BigDecimal.ZERO;
	
	@Column (name="HABER",nullable=false,scale=6,precision=16)
	private BigDecimal haber=BigDecimal.ZERO;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Poliza getPoliza() {
		return poliza;
	}

	public void setPoliza(Poliza poliza) {
		this.poliza = poliza;
	}

	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {		
		Object old=this.cuenta;
		this.cuenta = cuenta;
		firePropertyChange("cuenta", old, cuenta);
		
	}

	public BigDecimal getDebe() {
		if(debe==null)
			debe=BigDecimal.ZERO;
		return debe;
	}
	
	public CantidadMonetaria getDebeCM(){
		return CantidadMonetaria.pesos(getDebe());
	}

	public void setDebe(BigDecimal debe) {
		Object old=this.debe;
		this.debe = debe;
		firePropertyChange("debe", old, debe);
	}

	public BigDecimal getHaber() {
		if(haber==null)
			haber=BigDecimal.ZERO;
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		Object old=this.haber;
		this.haber = haber;
		firePropertyChange("haber", old, haber);
	}
	
	public CantidadMonetaria getHaberCM(){
		return CantidadMonetaria.pesos(getHaber());
	}

	public int getVersion() {
		return version;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		Object old=this.referencia;
		this.referencia = referencia;
		firePropertyChange("referencia", old, referencia);
	}

	public String getReferencia2() {
		return referencia2;
	}

	public void setReferencia2(String referencia2) {
		Object old=this.referencia2;
		this.referencia2 = referencia2;
		firePropertyChange("referencia2", old, referencia2);
		
	}
	
	public String getTipo(){
		if(getDebe().abs().doubleValue()>0)
			return "D";
		else if(getHaber().abs().doubleValue()>0)
			return "H";
		return "";
	}

	public String getAsiento() {
		return asiento;
	}

	public void setAsiento(String asiento) {
		this.asiento = asiento;
	}
	

	public ConceptoContable getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoContable concepto) {
		Object old=this.concepto;
		this.concepto = concepto;
		firePropertyChange("concepto", old, concepto);
		setDescripcion(concepto!=null?concepto.getDescripcion():null);
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + renglon;
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
		PolizaDet other = (PolizaDet) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (renglon != other.renglon)
			return false;
		return true;
	}



	@Transient
	private int year;
	@Transient
	private int mes;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		if(year==0 && (getPoliza()!=null)){
			year=Periodo.obtenerYear(getPoliza().getFecha());
		}
		this.year = year;
	}

	public int getMes() {
		if(mes==0 && (getPoliza()!=null))
			mes=Periodo.obtenerMes(getPoliza().getFecha())+1;
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public String getDescripcion2() {
		return descripcion2;
	}

	public void setDescripcion2(String descripcion2) {
		Object old=this.descripcion2;
		this.descripcion2 = descripcion2;
		firePropertyChange("descripcion2", old, descripcion2);
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0} Debe: {1} Haber:{2} - {3}"
				, getCuenta(),getDebe(),getHaber(),getAsiento());
	}

	

	@Transient
	private int renglon=0;
	
	public int getRenglon() {
		return renglon;
	}

	public void setRenglon(int renglon) {
		this.renglon = renglon;
	}
	

	@Transient
	private BigDecimal acumulado=BigDecimal.ZERO;

	public BigDecimal getAcumulado() {
		return acumulado;
	}

	public void setAcumulado(BigDecimal acumulado) {
		this.acumulado = acumulado;
	}

	
	
	

}

