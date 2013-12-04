package com.luxsoft.siipap.model.tesoreria;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Empresa;

@Entity
@Table (name="SW_BANCOS")
public class Banco extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="BANCO_ID")
	private Long id;
	
	@ManyToOne(optional=false)
    @JoinColumn(name="EMPRESA_ID", nullable=false,updatable=false)
	private Empresa empresa;
	
	@Column (name="CLAVE", nullable=false,unique=true,length=20)
	@Length (max=20)
	@NotEmpty (message="La clave es mandatoria")
	private String clave;
	
	@Length (max=100)
	private String nombre;
	
	@Length (max=20)
	private String rfc;
	
	@Length (max=100)
	private String contacto1;
	
	@Length (max=100)
	private String contacto2;
	
	@Email
	private String email1;
	
	@Email
	private String email2;
	
	@Column (nullable=false)
	private boolean nacional=true;
	
	@Type (type="date")
	private Date creado;
	
	//@Column(name="NOMBRE_SIIPAP",length=100,nullable=true)
	@Transient
	private String nombreSiipap;
	
	@Column(name="DIAS_INV_ISR")
	private Integer diasInversionIsr=365;
	
	
	public Banco() {}
	
	public Banco(String clave, String nombre) {	
		this.clave = clave;
		this.nombre = nombre;
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
		this.empresa = empresa;
	}
	
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public String getContacto1() {
		return contacto1;
	}
	public void setContacto1(String contacto1) {
		this.contacto1 = contacto1;
	}
	public String getContacto2() {
		return contacto2;
	}
	public void setContacto2(String contacto2) {
		this.contacto2 = contacto2;
	}
	
	public String getEmail1() {
		return email1;
	}
	public void setEmail1(String email1) {
		this.email1 = email1;
	}
	public String getEmail2() {
		return email2;
	}
	public void setEmail2(String email2) {
		this.email2 = email2;
	}
	public boolean isNacional() {
		return nacional;
	}
	public void setNacional(boolean nacional) {
		this.nacional = nacional;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		Object old=this.clave;
		this.clave = clave;
		support.firePropertyChange("clave", old, clave);
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((clave == null) ? 0 : clave.hashCode());
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
		final Banco other = (Banco) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		return true;
	}	
	
	public String toString(){
		return this.clave;
	}	
	public Date getCreado() {
		return creado;
	}
	
	public void setCreado(Date creado) {
		this.creado = creado;
	}

	public String getNombreSiipap() {
		return nombreSiipap;
	}

	public void setNombreSiipap(String nombreSiipap) {
		this.nombreSiipap = nombreSiipap;
	}

	public Integer getDiasInversionIsr() {
		return diasInversionIsr;
	}

	public void setDiasInversionIsr(Integer diasInversionIsr) {
		Object old=this.diasInversionIsr;
		this.diasInversionIsr = diasInversionIsr;
		firePropertyChange("diasInversionIsr", old, diasInversionIsr);
	}
	

	
	 
	
}
