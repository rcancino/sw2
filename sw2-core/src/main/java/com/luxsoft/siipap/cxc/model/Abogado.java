package com.luxsoft.siipap.cxc.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Pattern;
import org.hibernate.validator.Range;
import org.hibernate.validator.Valid;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;


@Entity
@Table(name="SW_ABOGADOS")
public class Abogado extends BaseBean{
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)	
	@Column(name="ABOGADO_ID")	
	private Long id;
	
	@Column(name="CLAVE",nullable=false,unique=true)
	@Range (min=1,max=99, message="Rango invalido (1 - 99)")
	private long clave;
	
	@Column(name="NOMBRE",length=40)
	@NotEmpty
	@Length(min=5, max=40,message="Longitud incorrecta (3 - 50)")
	private String nombre;
	
	@Column(name="TLEFONO",length=30)	
	@Length(max=30,message="Longitud máxima 30")
	private String telefono;
	
	@Embedded
	@Valid
	private Direccion direccion;
	
	private boolean activo=true;
	
	@Column(name="OBSERVACIONES")
	@Length(max=40,message="Longitud maximo de 40 caracteres")
	private String observaciones;
	
	@Email
	private String email;
	
	@Pattern(regex="^([A-Z\\s]{4})\\d{6}([A-Z\\w]{3})$")
	@Length(max=15)
	@Column(name="RFC")
	private String rfc;
	
	private Date baja;
		
	private Date alta=new Date();
	
	public Abogado() {		
	}

	public Abogado(long clave, String nombre) {		
		this.clave = clave;
		this.nombre = nombre;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public Date getAlta() {
		return alta;
	}

	public void setAlta(Date alta) {
		this.alta = alta;
	}

	public Date getBaja() {
		return baja;
	}

	public void setBaja(Date baja) {
		this.baja = baja;
	}

	public long getClave() {
		return clave;
	}

	public void setClave(long clave) {
		this.clave = clave;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}	

	public Direccion getDireccion() {
		return direccion;
	}

	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	

	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(!obj.getClass().isAssignableFrom(getClass())) return false;
		Abogado other=(Abogado)obj;
		return new EqualsBuilder()
			.append(clave,other.getClave())
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(clave)
			.toHashCode();
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
			.append(getClave())
			.append(getNombre())			
			.toString();
		
	}
	

}
