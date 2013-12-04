package com.luxsoft.sw3.maquila.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.NotNull;


import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Proveedor;


@SuppressWarnings("unchecked")
@Entity
@Table(name="SX_ALMACENES")
public class Almacen extends BaseBean implements Comparable{
	
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="ALMACEN_MAQ_ID", 
            allocationSize=1)
	@Id 
	@GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column(name="ALMACEN_ID",unique=true)
	private Long id;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)			
	@JoinColumn(name = "MAQUILADOR_ID", nullable = false)
	@NotNull(message="El maquilador es mandatorio")
	//private Maquilador maquilador;
	private Proveedor maquilador;
	
	@Column(name="NOMBRE",nullable=false,unique=true,updatable=false)
	private String nombre; 
	
	@Embedded
	private Direccion direccion=new Direccion();
	
	@Column(name="TELEFONO1",length=30)
	private String telefono1;
	
	@Column(name="TELEFONO2",length=30)
	private String telefono2;
	
	@Column(name="FAX",length=30)
	private String fax;
	
	private boolean matriz=false;
	
	public Almacen(){
		
	}
	
	public Almacen(Proveedor maquilador){
		setMaquilador(maquilador);
	}
	
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Direccion getDireccion() {
		return direccion;
	}
	public void setDireccion(Direccion direccion) {
		Object old=this.direccion;
		this.direccion = direccion;
		firePropertyChange("direccion", old, direccion);
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		Object old=this.fax;
		this.fax = fax;
		firePropertyChange("fax", old, fax);
	}
	
	public Proveedor getMaquilador() {
		return maquilador;
	}

	public void setMaquilador(Proveedor maquilador) {
		Object old=this.maquilador;
		this.maquilador = maquilador;
		firePropertyChange("maquilador", old, maquilador);
	}

	public boolean isMatriz() {
		return matriz;
	}
	public void setMatriz(boolean matriz) {
		this.matriz = matriz;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}
	public String getTelefono1() {
		return telefono1;
	}
	public void setTelefono1(String telefono1) {
		Object old=this.telefono1;
		this.telefono1 = telefono1;
		firePropertyChange("telefono1", old, telefono1);
	}
	public String getTelefono2() {
		return telefono2;
	}
	public void setTelefono2(String telefono2) {
		Object old=this.telefono2;
		this.telefono2 = telefono2;
		firePropertyChange("telefono2", old, telefono2);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(!obj.getClass().isAssignableFrom(obj.getClass())) return false;
		Almacen other=(Almacen)obj;
		return new EqualsBuilder()			
			.append(nombre,other.getNombre())
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(nombre)
			.toHashCode();
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getNombre())
		.toString();
	}

	public int compareTo(Object o) {
		if(o ==null)return -1;
		if(o==this) return 0;
		Almacen a=(Almacen)o;
		return getNombre().compareTo(a.getNombre());
	}

}
