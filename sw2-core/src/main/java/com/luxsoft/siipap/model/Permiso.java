package com.luxsoft.siipap.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.acegisecurity.GrantedAuthority;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Table(name="SX_PERMISOS")
public class Permiso extends BaseBean implements Serializable, GrantedAuthority ,Comparable<Permiso>{
	
	 private static final long serialVersionUID = 3690197650654059849L;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Version
	private int version;
	
	@Column(name="NOMBRE",nullable=false,length=50,unique=true)
	@NotNull @Length(min=5,max=50)
	private String nombre;
	
	@Column(name="DESCRIPCION",nullable=true,length=100)
	@Length(max=100)
	private String descripcion;
	
	@Enumerated(EnumType.STRING)
	@Column (name="MODULO",nullable=false,length=20)
	@NotNull
	private Modulos modulo;
	
	public Permiso(){}
	
	
	
	public Permiso(String nombre, String descripcion, Modulos modulo) {
		super();
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.modulo = modulo;
	}



	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Modulos getModulo() {
		return modulo;
	}
	public void setModulo(Modulos modulo) {
		this.modulo = modulo;
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
		result = PRIME * result + ((modulo == null) ? 0 : modulo.hashCode());
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
		final Permiso other = (Permiso) obj;
		if (modulo == null) {
			if (other.modulo != null)
				return false;
		} else if (!modulo.equals(other.modulo))
			return false;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}
	
	public String toString(){
		return modulo.name()+" - "+nombre;
	}



	/**
    * @see org.acegisecurity.GrantedAuthority#getAuthority()
    * @return the name property (getAuthority required by Acegi's GrantedAuthority interface)
    */
   @Transient
	public String getAuthority() {
		return this.nombre;
	}



	public int compareTo(Permiso o) {
		if(getModulo().equals(o.getModulo())){
			return getNombre().compareTo(o.getNombre());
		}
		return getModulo().compareTo(o.getModulo());
	}
		
	public boolean isAutorized(Object id){
		return getResource().equalsIgnoreCase(id.toString());
	}
	
	public String getResource(){
		//return getModulo()+"."+getNombre();
		return getNombre();
	}

}
