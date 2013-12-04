package com.luxsoft.siipap.model.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="SX_CORTES_MEDIDAS")
public class MedidaPorCorte {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "MEDIDA_ID")
	private Long id;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="CORTE_ID",nullable=false)	
	private Corte corte;
	
	@Column(name="CORTES")
	private int cortes;
	
	@Column(name="LONG_MIN")
	private double longitudMinima;
	
	@Column(name="LONG_MAX")
	private double longitudMaxima;

	
	
	public Corte getCorte() {
		return corte;
	}

	public void setCorte(Corte corte) {
		this.corte = corte;
	}

	public int getCortes() {
		return cortes;
	}

	public void setCortes(int cortes) {
		this.cortes = cortes;
	}

	public double getLongitudMinima() {
		return longitudMinima;
	}

	public void setLongitudMinima(double longitudMinima) {
		this.longitudMinima = longitudMinima;
	}

	public double getLongitudMaxima() {
		return longitudMaxima;
	}

	public void setLongitudMaxima(double longitudMaxima) {
		this.longitudMaxima = longitudMaxima;
	}

	public Long getId() {
		return id;
	}
	
	

}
