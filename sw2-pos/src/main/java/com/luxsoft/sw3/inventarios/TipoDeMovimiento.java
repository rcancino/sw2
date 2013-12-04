package com.luxsoft.sw3.inventarios;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;




/**
 * Entidad para clasificar los tipos de movimientos
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MOVI_TIPOS")
public class TipoDeMovimiento {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Column(name="CONCEPTO",length=3,unique=true,nullable=false) 
	private String concepto;
	
	@Column(name="SIN_AUT_MAX")
	private double maximoSinAutorizacion=-1;	

	@Column(name="DESCRIPCION",nullable=false)
	private String descripcion;
	
	

	public Long getId() {
		return id;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public double getMaximoSinAutorizacion() {
		return maximoSinAutorizacion;
	}

	public void setMaximoSinAutorizacion(double maximoSinAutorizacion) {
		this.maximoSinAutorizacion = maximoSinAutorizacion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
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
		TipoDeMovimiento other = (TipoDeMovimiento) obj;
		if (concepto == null) {
			if (other.concepto != null)
				return false;
		} else if (!concepto.equals(other.concepto))
			return false;
		return true;
	}
	
	public String toString(){
		return concepto;
	}

}
