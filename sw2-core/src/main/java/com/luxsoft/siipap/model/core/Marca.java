package com.luxsoft.siipap.model.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.luxsoft.siipap.model.BaseBean;

/**
 * Catalogo de marcas
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MARCAS")
public class Marca extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="MARCA_ID")
	private Long id;
	
	@Column(name="NOMBRE",length=30,nullable=false,unique=true)
	private String nombre;
	
	public Marca(){}
	
	public Marca(final String nobmre){
		this.nombre=nobmre;
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
		final Marca other = (Marca) obj;
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
	
	

}
