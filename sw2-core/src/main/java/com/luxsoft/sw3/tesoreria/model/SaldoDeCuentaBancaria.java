package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import com.luxsoft.siipap.model.tesoreria.Cuenta;

@Entity
@Table (name="SW_CUENTAS_SALDOS")
public class SaldoDeCuentaBancaria {
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="SALDO_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="CUENTA_ID", nullable=false) 
    @NotNull
	private Cuenta cuenta;
	
	@Column(name="ANO",nullable=false)
    @NotNull
	private int year;
	
	@Column(name="MES",nullable=false)
	@NotNull 
	@Range(min=1,max=12)
	private int mes;	
	
	@Column (name="DEPOSITOS",nullable=false,scale=6,precision=16)
	private BigDecimal depositos=BigDecimal.ZERO;
	
	@Column (name="RETIROS",nullable=false,scale=6,precision=16)
	private BigDecimal retiros=BigDecimal.ZERO;
	
	@Column (name="SALDO_INICIAL",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal saldoInicial=BigDecimal.ZERO;
	
	@Column (name="SALDO_FINAL",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal saldoFinal=BigDecimal.ZERO;
	
	@Column(name="CIERRE",nullable=true)
	private Date cierre;
	
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

	public Cuenta getCuenta() {
		return cuenta;
	}

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
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

	public BigDecimal getDepositos() {
		return depositos;
	}

	public void setDepositos(BigDecimal depositos) {
		this.depositos = depositos;
	}

	public BigDecimal getRetiros() {
		return retiros;
	}

	public void setRetiros(BigDecimal retiros) {
		this.retiros = retiros;
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

	public Date getCierre() {
		return cierre;
	}

	public void setCierre(Date cierre) {
		this.cierre = cierre;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cuenta == null) ? 0 : cuenta.hashCode());
		result = prime * result + mes;
		result = prime * result + year;
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
		SaldoDeCuentaBancaria other = (SaldoDeCuentaBancaria) obj;
		if (cuenta == null) {
			if (other.cuenta != null)
				return false;
		} else if (!cuenta.equals(other.cuenta))
			return false;
		if (mes != other.mes)
			return false;
		if (year != other.year)
			return false;
		return true;
	}
		

	public String toString(){
		String pattern="Cta: {0} Per:{1}/{2} Saldo Final:{3}";
		return MessageFormat.format(pattern, getCuenta().getClave(),getYear(),getMes(),getSaldoFinal());
	}
	
	public void actualizar(){
		BigDecimal res=BigDecimal.ZERO;
		res=res.add(getSaldoInicial()).add(getDepositos()).subtract(getRetiros());
		setSaldoFinal(res);
	}
	
}
