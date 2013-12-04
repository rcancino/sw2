package com.luxsoft.siipap.ventas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Catalogo de tipo de lista de precios
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_LP_TIPOS")
public class TipoDeLista extends BaseBean{
	
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="LISTA_ID")
	private Long id;
	
	@Column(name="NOMBRE",nullable=false,length=20)
	@NotNull @Length(max=20)
	private String nombre;
	
	@Column(name="DESCRIPCION")
	@Length(max=200)
	private String descripcion;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj==this) return true;
		
		if (getClass() != obj.getClass())
			return false;		
		TipoDeLista otro=(TipoDeLista)obj;
		return new EqualsBuilder()
		.append(getNombre(), otro.getNombre())
		.isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getNombre())
		.toHashCode();
	}
	
	public String toString(){
		return getNombre();
	}
		

}
