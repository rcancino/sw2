package com.luxsoft.sw3.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Cliente autorizado para la venta tipo check plus
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CHECKPLUS_OPCION")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CheckPlusOpcion {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	protected String id;
	
	@Version
	private int version;
	
	@Column(name="PLAZO",nullable=false)
	private int plazo;
	
	@Column(name="CARGO",nullable=false)
	private BigDecimal cargo;
	
	private String comentario;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=false,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=false,insertable=true,updatable=true))
	   })
	private UserLog log;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=false,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=false,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=false,insertable=true,updatable=true))
	   })
	private AdressLog addresLog;

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

	

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
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

	public int getPlazo() {
		return plazo;
	}

	public void setPlazo(int plazo) {
		this.plazo = plazo;
	}

	public BigDecimal getCargo() {
		return cargo;
	}

	public void setCargo(BigDecimal cargo) {
		this.cargo = cargo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + plazo;
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
		CheckPlusOpcion other = (CheckPlusOpcion) obj;
		if (plazo != other.plazo)
			return false;
		return true;
	}

	public String toString(){
		return MessageFormat.format("{0} D’as (-{1})", plazo,cargo);
	}
}
