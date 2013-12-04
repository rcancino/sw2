package com.luxsoft.siipap.model;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * Nuevo modelo de autorizacion basado en Autorizacion
 * 
 * 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_AUTORIZACIONES2")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
		name="TIPO"
		,discriminatorType=DiscriminatorType.STRING,length=20)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
@DiscriminatorValue("GENERICA")
public  class Autorizacion2 implements Serializable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="AUT_ID")
	private String id;
	
	@Column (name="AUT_FECHA",nullable=true)
	@Type (type="timestamp")
	private Date fechaAutorizacion=new Date();
	
	@Column(name="AUT_VENCIMIENTO",nullable=true)
	private Date vencimiento;
	
    @JoinColumn(name="AUT_USUARIO",  nullable=false,updatable=false)	
	private String autorizo; 
	
	@Column (name="AUT_COMMENTARIO",nullable=true)
	private String comentario;
	
	
	@Column(name="AUT_CREADO",updatable=false,insertable=false)
	@Generated(GenerationTime.INSERT)
	private Date creado;
	
	
	@Column(name="AUT_MODIFICADO",updatable=false,insertable=false)
	@Generated(GenerationTime.ALWAYS)
	private Date modificado;
	
	@Column(name="IP_ADRESS",length=30)
	private String ipAdress;
	
	@Column(name="MAC_ADRESS",length=30)
	private String macAdress;
	
	public Autorizacion2(){}

	public Date getFechaAutorizacion() {
		return fechaAutorizacion;
	}

	public void setFechaAutorizacion(Date fechaAutorizacion) {
		this.fechaAutorizacion = fechaAutorizacion;
	}

	public Date getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}

	public String getAutorizo() {
		return autorizo;
	}

	public void setAutorizo(String autorizo) {
		this.autorizo = autorizo;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	

	public String getId() {
		return id;
	}
	

	public Date getCreado() {
		return creado;
	}

	public Date getModificado() {
		return modificado;
	}

	public  String getInfo(){
		String pattern="AUTORIZADO";
		if(getVencimiento()!=null){
			pattern+=" Vig: {0,date,short}";
			return MessageFormat.format(pattern,getVencimiento());
		}else
			return pattern;
	}
	
	
	
	
	public String getIpAdress() {
		return ipAdress;
	}

	public void setIpAdress(String ipAdress) {
		this.ipAdress = ipAdress;
	}
	
	

	public String getMacAdress() {
		return macAdress;
	}

	public void setMacAdress(String macAdress) {
		this.macAdress = macAdress;
	}

	public String toString(){
		String pattern="{0} {1} {2}";
		return MessageFormat.format(pattern, this.autorizo,this.comentario,this.fechaAutorizacion);
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
		Autorizacion2 other = (Autorizacion2) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * Determina si la autorizacion es vigente 
	 * Compara el vencimiento contra la fecha indicada
	 * trunca a dia, es decir no toma en cuenta la hora ni minutos
	 * 
	 * @param fecha
	 * @return
	 */
	public boolean isVigente(final Date fecha){
		if(getVencimiento()!=null){
			Date vence=DateUtils.truncate(getVencimiento(), Calendar.DATE);
			Date today=DateUtils.truncate(fecha,  Calendar.DATE);
			return vence.compareTo(today)>=0;
		}
		return true;
	}
	
	
	

}
