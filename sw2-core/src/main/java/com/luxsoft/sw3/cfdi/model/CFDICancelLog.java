package com.luxsoft.sw3.cfdi.model;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.UserLog;


@Entity
@Table(name="SX_CFDI_CANCEL_LOG")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CFDICancelLog {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="CFD_ID")
	protected String id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="CFDI_ID", nullable=false,updatable=false) 
    @NotNull
	private CFDI cfdi;
	
	@Column(name="MESSAGE",nullable=true,length=1048576)
	private byte[] message;
	
	@Column(name="SAT_AKA",nullable=true,length=1048576)
	private byte[] satAka;
	
	@Column(name="CANCELACION",nullable=false)
	public Date cancelacion;
	
	@Column(name="COMENTARIO",nullable=true)
	public String comentario;
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	
	private UserLog log=new UserLog();

	public CFDI getCfdi() {
		return cfdi;
	}
	public void setCfdi(CFDI cfdi) {
		this.cfdi = cfdi;
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

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public byte[] getSatAka() {
		return satAka;
	}

	public void setSatAka(byte[] satAka) {
		this.satAka = satAka;
	}

	public Date getCancelacion() {
		return cancelacion;
	}

	public void setCancelacion(Date cancelacion) {
		this.cancelacion = cancelacion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	public void setLog(UserLog log) {
		this.log = log;
	}
	public UserLog getLog() {
		return log;
	}
	

}
