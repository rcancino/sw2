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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

/**
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SX_CONTABILIDAD_SALDOSDET"
	,uniqueConstraints=@UniqueConstraint(
			columnNames={"CONCEPTO_ID","YEAR","MES"})
	)
public class SaldoDeCuentaPorConcepto {

	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="SALDODET_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="SALDO_ID",nullable=false,updatable=false)	
	private SaldoDeCuenta saldo;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="CONCEPTO_ID", nullable=false) 
    @NotNull
	private ConceptoContable concepto;
	
	@Column(name="YEAR",nullable=false)
    @NotNull
	private int year;
	
	@Column(name="MES",nullable=false)
	@NotNull 
	@Range(min=1,max=13)
	private int mes;	
	
	@Column (name="DEBE",nullable=false,scale=6,precision=16)
	private BigDecimal debe=BigDecimal.ZERO;
	
	@Column (name="HABER",nullable=false,scale=6,precision=16)
	private BigDecimal haber=BigDecimal.ZERO;
	
	@Column (name="SALDO_INICIAL",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal saldoInicial=BigDecimal.ZERO;
	
	@Column (name="SALDO_FINAL",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal saldoFinal=BigDecimal.ZERO;
	
	public SaldoDeCuentaPorConcepto() {}
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public SaldoDeCuenta getSaldo() {
		return saldo;
	}

	public void setSaldo(SaldoDeCuenta saldo) {
		this.saldo = saldo;
	}

	public ConceptoContable getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoContable concepto) {
		this.concepto = concepto;
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

	public BigDecimal getDebe() {
		if(debe==null)
			return BigDecimal.ZERO;
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		this.debe = debe;
	}

	public BigDecimal getHaber() {
		if(haber==null){
			return BigDecimal.ZERO;
		}
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		this.haber = haber;
	}

	public BigDecimal getSaldoInicial() {
		return saldoInicial;
	}

	public void setSaldoInicial(BigDecimal saldoInicial) {
		this.saldoInicial = saldoInicial;
	}

	public BigDecimal getSaldoFinal() {
		return saldoFinal;
	}

	public void setSaldoFinal(BigDecimal saldoFinal) {
		this.saldoFinal = saldoFinal;
	}

	
	public String toString(){
		String pattern="Concepto: {0} " +
				" S.Incial: {1} " +
				" Cargos: {2} " +
				" Abonos:{3} " +
				" Saldo F.{4}" +
				" Per:{5}/{6} ";
		return MessageFormat.format(pattern
				, getConcepto().getClave()
				,getSaldoInicial()
				,getDebe()
				,getHaber()
				,getSaldoFinal()
				,getYear()
				,getMes()
				);
	}
	
	public void actualizar(){
		BigDecimal movimientos=getDebe().subtract(getHaber());
		setSaldoFinal(getSaldoInicial().add(movimientos));
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((concepto == null) ? 0 : concepto.hashCode());
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
		SaldoDeCuentaPorConcepto other = (SaldoDeCuentaPorConcepto) obj;
		if (concepto == null) {
			if (other.concepto != null)
				return false;
		} else if (!concepto.equals(other.concepto))
			return false;
		return true;
	}


	
	
	
}
