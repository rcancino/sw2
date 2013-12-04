package com.luxsoft.siipap.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

@Entity
@Table (name="SW_SUCURSALES")
public class Sucursal extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="SUCURSAL_ID")
	private Long id;
	
	@ManyToOne(optional=false)
    @JoinColumn(name="EMPRESA_ID", nullable=false,updatable=false)
	private Empresa empresa;
	
	@Column (name="CLAVE", nullable=false, unique=true,length=20)
	private int clave;
	
	@Column (name="NOMBRE",nullable=false, length=50)
	private String nombre;
	
	@Column (name="HABILITADA",nullable=false)
	private boolean habilitada=true;
	
	@Column (name="FOLIO_INV",nullable=false)
	private long folioInventario=0;
	
	@Embedded
	private Direccion direccion=new Direccion()	;
	
	public Sucursal(){}
	
	public Sucursal(int clave, String nombre) {
		super();
		this.clave = clave;
		this.nombre = nombre;
	}

	public Sucursal(final Empresa empresa,int clave, String nombre) {
		super();
		this.clave = clave;
		this.nombre = nombre;
		this.empresa=empresa;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Empresa getEmpresa() {
		return empresa;
	}
	public void setEmpresa(Empresa empresa) {
		Object old=this.empresa;
		this.empresa = empresa;
		firePropertyChange("empresa", old, empresa);
	}

	public int getClave() {
		return clave;
	}
	public void setClave(int clave) {
		int old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}	

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + clave;
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
		final Sucursal other = (Sucursal) obj;
		if (clave != other.clave)
			return false;
		return true;
	}
	
	public String toString(){
		return this.nombre;
	}

	public boolean isHabilitada() {
		return habilitada;
	}
	public void setHabilitada(boolean habilitada) {
		boolean old=this.habilitada;
		this.habilitada = habilitada;
		firePropertyChange("habilitada", old, habilitada);
	}
	
	public int getClaveContable(){
		if(getClave()==10)
			return 6;
		else if(getClave()==9)
			return 10;
		else if(getClave()==1)
			return 0;
		else if(getClave()==0)
			return 1;
		return getClave();
	}
	
	public String getClaveContableAsString(){
		String clave=String.valueOf(getClaveContable());
		return StringUtils.leftPad(clave, 3,'0');
	}

	public long getFolioInventario() {
		return folioInventario;
	}

	public void setFolioInventario(long folioInventario) {
		this.folioInventario = folioInventario;
	}

	public Direccion getDireccion() {
		if(direccion==null){
			direccion=new Direccion();
		}
		return direccion;
	}

	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}
	
	

}
