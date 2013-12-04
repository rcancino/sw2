package com.luxsoft.siipap.model;

import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.luxsoft.siipap.util.DateUtil;

/**
 * Encapsula el estado y comportamiento de una autorizacion   
 * Habilitada para persistencia JPA para relaciones de composición
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SW_AUTORIZACIONES")
public class Autorizacion {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="AUT_ID")
	private Long id;
	
	@Column (name="AUT_FECHA",nullable=true)
	@Type (type="timestamp")
	private Date fechaAutorizacion=new Date();
	
	@ManyToOne(optional=true)
    @JoinColumn(name="AUT_USERID",  nullable=true,updatable=false)	
	private User autorizo; 
	
	@Column (name="AUT_COMMENTARIO", length=80)
	private String comentario;
	
	public Autorizacion(){}	

	public Autorizacion(Date fechaAutorizacion, User autorizo) {
		super();
		this.fechaAutorizacion = fechaAutorizacion;
		this.autorizo = autorizo;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public User getAutorizo() {
		return autorizo;
	}
	public void setAutorizo(User autorizo) {
		this.autorizo = autorizo;
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Date getFechaAutorizacion() {
		return fechaAutorizacion;
	}
	public void setFechaAutorizacion(Date fechaAutorizacion) {
		this.fechaAutorizacion = fechaAutorizacion;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((autorizo == null) ? 0 : autorizo.hashCode());
		result = PRIME * result + ((fechaAutorizacion == null) ? 0 : fechaAutorizacion.hashCode());
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
		final Autorizacion other = (Autorizacion) obj;
		if (autorizo == null) {
			if (other.autorizo != null)
				return false;
		} else if (!autorizo.equals(other.autorizo))
			return false;
		if (fechaAutorizacion == null) {
			if (other.fechaAutorizacion != null)
				return false;
		} else if (!fechaAutorizacion.equals(other.fechaAutorizacion))
			return false;
		return true;
	}
	
	
	public String toString(){
		String pattern="{0} {1}";
		return MessageFormat.format(pattern, this.autorizo.getFullName(),DateUtil.convertDateToString(this.getFechaAutorizacion()));
	}

}
