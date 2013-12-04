package com.luxsoft.siipap.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Table (name="SW_DEPARTAMENTOS")
public class Departamento extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="DEPTO_ID")
	private Long id;
	
	@Column (name="CLAVE", nullable=false,length=20,unique=true)
	@NotNull @Length(min=4,max=20)
	private String clave;
	
	@Column (name="DESCRIPCION",length=50)
	@NotNull @Length(min=4,max=50)
	private String descripcion;
	
	public Departamento() {}
	
	public Departamento(String clave, String descripcion) {		
		this.clave = clave;
		this.descripcion = descripcion;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		Object old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		Object old=this.descripcion;
		this.descripcion = descripcion;
		firePropertyChange("descripcion", old, descripcion);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clave == null) ? 0 : clave.hashCode());
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
		final Departamento other = (Departamento) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		return true;
	}
	
	public String toString(){
		return getClave();
	}
	

}
