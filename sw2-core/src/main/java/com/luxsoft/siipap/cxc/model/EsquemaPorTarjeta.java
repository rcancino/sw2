package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Parent;
import org.hibernate.validator.NotNull;


@Embeddable
public class EsquemaPorTarjeta implements Serializable{
	
	@Column(name="COMISION_BANCARIA",nullable=false)
	@NotNull
	private double comisionBancaria=0;
	
	@Column(name="COMISION_VENTA",nullable=false)
	@NotNull
	private double comisionVenta=0;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="ESQUEMA_ID",nullable=false)
	@NotNull
	private Esquema esquema;
	
	@Parent
	private Tarjeta tarjeta;
	
	public EsquemaPorTarjeta() {}

	public EsquemaPorTarjeta(Esquema esquema,double comision) {
		this.esquema = esquema;
		this.comisionBancaria = comision;
	}

	public Tarjeta getTarjeta() {
		return tarjeta;
	}

	public void setTarjeta(Tarjeta tarjeta) {
		this.tarjeta = tarjeta;
	}

	public double getComisionBancaria() {
		return comisionBancaria;
	}

	public void setComisionBancaria(double comision) {
		this.comisionBancaria = comision;
	}
	
	public double getComisionVenta() {
		return comisionVenta;
	}

	public void setComisionVenta(double comisionVenta) {
		this.comisionVenta = comisionVenta;
	}

	public Esquema getEsquema() {
		return esquema;
	}

	public void setEsquema(Esquema esquema) {
		this.esquema = esquema;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((esquema == null) ? 0 : esquema.hashCode());
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
		EsquemaPorTarjeta other = (EsquemaPorTarjeta) obj;
		if (esquema == null) {
			if (other.esquema != null)
				return false;
		} else if (!esquema.equals(other.esquema))
			return false;
		return true;
	}

	public String toString(){
		return esquema.toString();
	}

}
