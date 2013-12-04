package com.luxsoft.sw3.ventas;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.tesoreria.Banco;

/**
 * Cliente autorizado para la venta tipo check plus
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CHECKPLUS_REFBANCOS")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CheckPlusReferenciaBancaria {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="CLIENTE_CHECKPLUS_ID",nullable=false,updatable=false)	
	private CheckPlusCliente cliente;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
    @JoinColumn(name="BANCO_ID", nullable=false,updatable=false)
    @NotNull (message="El Banco es obligatorio")
	private Banco banco;
	
	@Column(name="SUCURSAL",length=255)
	@Length (max=255)
	@NotNull
	private String sucursal;
	
	 @Embedded
	 @NotNull
	 private Direccion direccion=new Direccion();
	
	@Column(name="NUMERO_DE_CUENTA",length=255)
	@Length (max=255)
	@NotNull
	private String numeroDeCuenta;
	
	@Column(name = "FECHA_APERTURA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fechaApertura;
	
	@Column(name="EJECUTIVO",length=255)
	@Length (max=255)
	@NotNull
	private String ejecutivo;
	
	@Column(name="TELEFONO",length=30)
	@Length (max=30)
	@NotNull
	private String telefono;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}
	

	public CheckPlusCliente getCliente() {
		return cliente;
	}

	public void setCliente(CheckPlusCliente cliente) {
		this.cliente = cliente;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public Direccion getDireccion() {
		return direccion;
	}

	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}

	public String getNumeroDeCuenta() {
		return numeroDeCuenta;
	}

	public void setNumeroDeCuenta(String numeroDeCuenta) {
		this.numeroDeCuenta = numeroDeCuenta;
	}

	public Date getFechaApertura() {
		return fechaApertura;
	}

	public void setFechaApertura(Date fechaApertura) {
		this.fechaApertura = fechaApertura;
	}

	public String getEjecutivo() {
		return ejecutivo;
	}

	public void setEjecutivo(String ejecutivo) {
		this.ejecutivo = ejecutivo;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((banco == null) ? 0 : banco.hashCode());
		result = prime * result
				+ ((numeroDeCuenta == null) ? 0 : numeroDeCuenta.hashCode());
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
		CheckPlusReferenciaBancaria other = (CheckPlusReferenciaBancaria) obj;
		if (banco == null) {
			if (other.banco != null)
				return false;
		} else if (!banco.equals(other.banco))
			return false;
		if (numeroDeCuenta == null) {
			if (other.numeroDeCuenta != null)
				return false;
		} else if (!numeroDeCuenta.equals(other.numeroDeCuenta))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return " [Banco=" + banco
				+ ", #=" + numeroDeCuenta + "]";
	}
	
	
	
}
