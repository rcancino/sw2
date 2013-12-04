package com.luxsoft.siipap.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;



import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
@Table(name="SW_EMPRESAS")
public class Empresa extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column (name="EMPRESA_ID")
	private Long id;
	
	@Column (name="CLAVE",unique=true,nullable=false,length=15)
	@NotNull @Length(min=4,max=15)
	private String clave;
	
	@Column (name="NOMBRE",unique=true,nullable=false,length=255)
	@NotNull @Length(min=4,max=255)
	private String nombre;
	
	@Column (name="DESCRIPCION",nullable=true,length=255)
	private String descripcion;
	
	@Column(name="RFC",nullable=false,length=14)
	@NotNull @Length(max=14)
	private String rfc;
	
	@Column (name="REGIMEN",nullable=true,length=255)
	private String regimen;
	
	@Embedded
	private Direccion direccion=new Direccion()	;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "COMPROBANTE_FISCAL", nullable = true, length = 10)
	private TipoComprobante tipoDeComprobante;
	
	@Column(name="NO_CERTIFICADO",nullable=true,length=20,unique=true)
	@Length(min=1,max=20,message="No de certificado invalido")
	@NotEmpty
	private String numeroDeCertificado;
	
	@Column(name = "CFDI_PK",  nullable = true, length = 100000)
	private byte[] cfdiPrivateKey;
	
	@Column(name = "CERTIFICADO_DIGITAL",  nullable = true, length = 100000)
	private byte[] certificadoDigital;
	
	public Empresa() {}
	

	public Empresa(String clave, String nombre) {		
		this.clave = clave;
		this.nombre = nombre;
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

	public Direccion getDireccion() {
		return direccion;
	}
	public void setDireccion(Direccion direccion) {
		Object old=this.direccion;
		this.direccion = direccion;
		firePropertyChange("direccion", old, direccion);
	}
	

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}

	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		Object old=this.rfc;
		this.rfc = rfc;
		firePropertyChange("rfc", old, rfc);
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
		final Empresa other = (Empresa) obj;
		if (nombre == null) {
			if (other.nombre != null)
				return false;
		} else if (!nombre.equals(other.nombre))
			return false;
		return true;
	}
	
	public String toString(){
		return getNombre()+" ("+getClave()+")";
	}


	public String getRegimen() {
		return regimen;
	}


	public void setRegimen(String regimen) {
		this.regimen = regimen;
	}
	
	
	

	public TipoComprobante getTipoDeComprobante() {
		return tipoDeComprobante;
	}


	public void setTipoDeComprobante(TipoComprobante tipoDeComprobante) {
		this.tipoDeComprobante = tipoDeComprobante;
	}


	public String getNumeroDeCertificado() {
		return numeroDeCertificado;
	}


	public void setNumeroDeCertificado(String numeroDeCertificado) {
		this.numeroDeCertificado = numeroDeCertificado;
	}

	


	public byte[] getCfdiPrivateKey() {
		return cfdiPrivateKey;
	}


	public void setCfdiPrivateKey(byte[] cfdiPrivateKey) {
		this.cfdiPrivateKey = cfdiPrivateKey;
	}


	public byte[] getCertificadoDigital() {
		return certificadoDigital;
	}


	public void setCertificadoDigital(byte[] certificadoDigital) {
		this.certificadoDigital = certificadoDigital;
	}




	public static enum TipoComprobante{
		CFD,CFDI
	}
	 

}
