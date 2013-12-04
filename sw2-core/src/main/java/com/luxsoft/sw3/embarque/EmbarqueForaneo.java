package com.luxsoft.sw3.embarque;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.validator.Length;

@Entity
@DiscriminatorValue("FORANEO")
public class EmbarqueForaneo extends Embarque{
	
	@Column(name="ZFORANEO_REGION",nullable=true,length=50)
	@Length(max=50)
	private String zona;
	
	@Column(name="ZFORANEO_COSTO",nullable=true)
	private BigDecimal costo=BigDecimal.ZERO;
	
	@Column(name="ZFORANEO_EMBARCADO",nullable=true)
	private Date embarcado;

	public String getZona() {
		return zona;
	}

	public void setZona(String zona) {
		this.zona = zona;
	}

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

	public Date getEmbarcado() {
		return embarcado;
	}

	public void setEmbarcado(Date embarcado) {
		this.embarcado = embarcado;
	}

}
