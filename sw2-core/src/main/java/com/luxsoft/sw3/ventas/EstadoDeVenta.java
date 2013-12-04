package com.luxsoft.sw3.ventas;

import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
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

import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Estado de las ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_VENTA_ESTADO")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class EstadoDeVenta {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false
		,fetch=FetchType.EAGER)
	@JoinColumn(name = "VENTA_ID", nullable = false,unique=true)
	private Venta venta;
	
	
	@Column(name="CORTADO",nullable=true)
	private Date cortado;
	
	@Column(name="SURTIDO",nullable=true)
	private Date surtido;
	
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog;	

	
	public EstadoDeVenta() {
		addresLog=new AdressLog();
		log=new UserLog();
	}
	
	public EstadoDeVenta(Venta venta) {
		this.venta = venta;
		addresLog=new AdressLog();
		log=new UserLog();
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public int getVersion() {
		return version;
	}


	public void setVersion(int version) {
		this.version = version;
	}


	public Venta getVenta() {
		return venta;
	}


	public void setVenta(Venta venta) {
		this.venta = venta;
	}


	public Date getCortado() {
		return cortado;
	}


	public void setCortado(Date cortado) {
		this.cortado = cortado;
	}


	public Date getSurtido() {
		return surtido;
	}


	public void setSurtido(Date surtido) {
		this.surtido = surtido;
	}


	public UserLog getLog() {
		return log;
	}


	public void setLog(UserLog log) {
		this.log = log;
	}


	public AdressLog getAddresLog() {
		return addresLog;
	}


	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}


	public String toString(){
		return MessageFormat.format("{0} ({1})  ", venta.getDocumento(),venta.getCliente());
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		EstadoDeVenta other = (EstadoDeVenta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
