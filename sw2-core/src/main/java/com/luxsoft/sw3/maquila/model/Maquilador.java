package com.luxsoft.sw3.maquila.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;


@Entity
@Table(name="SX_MAQUILADORES")
public class Maquilador extends BaseBean{
	
	@Id @Column(name="MAQUILADOR_ID",unique=true)
	private Long id;
	
	@Column(name="CLAVE",nullable=false,length=10,unique=true)
	private String clave;
	
	@Column(name="NOMBRE",nullable=false,length=70)
	private String nombre;
	
	@Embedded
	private Direccion direccion=new Direccion();
	
	@Column(name="TELEFONO1",length=30)
	private String telefono1;
	
	@Column(name="TELEFONO2",length=30)
	private String telefono2;
	
	@Column(name="FAX",length=30)
	private String fax;
	
	@Column(name="RFC",length=20)
	private String rfc="";
	
	@Column(name="DIAS_DE_CREDITO")
	private int diasDeCredito; //Dias de plazo para pago,Dias de credito que ofrece el proveedor
	
	@Column(name="VTO")
	private int vencimientoEstipulado; // A partid de la fecha de revision o a partir de la fecha de factura
	
	@Column(name="OBSERVACIONES")
	private String observaciones;
	
	@Column(name="REPRESENTANTE",length=30)
	@Length(max=30)
	private String representante;
	
	@Column(name = "CUENTACONTABLE", nullable = true, unique = false, length = 30)
    @Length (max=30,message="El rango maximo es de 30 caracteres")
	private String cuentaContable;
	
	@Column(name="TARIFA")
	private BigDecimal tarifa;
	
	@Column(name="ACTIVO")
	private boolean activo;
	
	
	public Maquilador(){
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
	
	public String getClave() {
		return clave;
	}
	
	public void setClave(String clave) {
		this.clave = clave;
	}
	
	public String getCuentaContable() {
		return cuentaContable;
	}
	
	public void setCuentaContable(String cuentaContable) {
		this.cuentaContable = cuentaContable;
	}
	
	public int getDiasDeCredito() {
		return diasDeCredito;
	}
	
	public void setDiasDeCredito(int diasDeCredito) {
		this.diasDeCredito = diasDeCredito;
	}
	public Direccion getDireccion() {
		return direccion;
	}
	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
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
	public String getRepresentante() {
		return representante;
	}
	public void setRepresentante(String representante) {
		this.representante = representante;
	}
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public String getTelefono1() {
		return telefono1;
	}
	public void setTelefono1(String telefono1) {
		this.telefono1 = telefono1;
	}
	public String getTelefono2() {
		return telefono2;
	}
	public void setTelefono2(String telefono2) {
		this.telefono2 = telefono2;
	}
	public int getVencimientoEstipulado() {
		return vencimientoEstipulado;
	}
	public void setVencimientoEstipulado(int vencimientoEstipulado) {
		this.vencimientoEstipulado = vencimientoEstipulado;
	}
	
	

	public BigDecimal getTarifa() {
		return tarifa;
	}

	public void setTarifa(BigDecimal tarifa) {
		this.tarifa = tarifa;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(!obj.getClass().isAssignableFrom(getClass())) return false;
		Maquilador other=(Maquilador)obj;
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
