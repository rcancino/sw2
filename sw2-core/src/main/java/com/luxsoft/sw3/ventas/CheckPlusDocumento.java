package com.luxsoft.sw3.ventas;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

/**
 * Documento asociado a un cliente tipo check plus
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CHECKPLUS_DOCTOS")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class CheckPlusDocumento {
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	protected String id;
	
	@Version
	private int version;
	 
	@ManyToOne(optional=false)
	@JoinColumn (name="CLIENTE_CHECKPLUS_ID",nullable=false,updatable=false)	
	private CheckPlusCliente cliente;
	
	@Column(name="tipo")
	@NotNull @Length(max=40)
	private String tipo;
	
	@Column(name="DESCRIPCION")
	@NotNull @Length(max=255)
	private String descripcion;
	
	@Column(name="URL")
	@NotNull @Length(max=255)
	private String url;

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

	public CheckPlusCliente getCliente() {
		return cliente;
	}

	public void setCliente(CheckPlusCliente cliente) {
		this.cliente = cliente;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((descripcion == null) ? 0 : descripcion.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
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
		CheckPlusDocumento other = (CheckPlusDocumento) obj;
		if (descripcion == null) {
			if (other.descripcion != null)
				return false;
		} else if (!descripcion.equals(other.descripcion))
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return " [Tipo=" + tipo + ", Descripcion="
				+ descripcion + "]";
	}
	
	
	
}
