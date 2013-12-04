package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.NotNull;

/**
 * Esquema para el pago con tarjeta 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_ESQUEMAS")
public class Esquema implements Serializable{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ESQUEMA_ID")
	private Long id;
	
	@Column(name="DESCRIPCION",nullable=false,unique=true)
	@NotNull
	private String descripcion;
	
	public Esquema() {}

	public Esquema(String nombre) {
		this.descripcion=nombre;
	}

	public Long getId() {
		return id;
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
				+ ((descripcion == null) ? 0 : descripcion.hashCode());
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
		Esquema other = (Esquema) obj;
		if (descripcion == null) {
			if (other.descripcion != null)
				return false;
		} else if (!descripcion.equals(other.descripcion))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return StringUtils.abbreviate(descripcion,50);
	}
	
	
	
	

}
