package com.luxsoft.siipap.model.core;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Clasificacion alterna para productos/servicios
 * 
 * Principalmente para compatibilidad. No se recomienda el uso
 * en nuevas instalaciones
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_CLASES")
public class Clase extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="CLASE_ID")
	private Long id;
	
	@Column(name="NOMBRE",length=30,nullable=false,unique=true)
	private String nombre;
	
	@ManyToOne(optional=true,fetch=FetchType.LAZY,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn (name="CORTE_ID")
	private Corte corte;
	
	public Clase(){}
	
	public Clase(String nombre){
		this.nombre=nombre;
	}

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
	
	
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((nombre == null) ? 0 : nombre.hashCode());
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
		final Clase other = (Clase) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}
	
	@Override
	public String toString() {		
		return nombre;
	}
	
	public Corte getCorte() {
		return corte;
	}
	public void setCorte(Corte corte) {
		this.corte = corte;
	}
	

}
