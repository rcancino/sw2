package com.luxsoft.siipap.model.core;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;

/**
 * JavaBean para represnetar un codigo numerico para un producto
 * Puede ser usado para codigos de barras o para codigos personalizados
 * de proveedores
 * 
 * @author Ruben Cancino
 *
 */
@Embeddable
public class Codigo extends BaseBean{
	
	@NotNull @Length(max=25)
	@Column(name="CLAVE",nullable=false,length=25)
	private String clave;
	
	@NotNull @Length(max=30) 
	@Column(name="CODIGO",nullable=false,length=30)
	private String codigo;
	
	@Length(max=255)
	@Column(name="COMENTARIO",length=255)
	private String comentario;
	
	public Codigo(){
		
	}

	public Codigo(String clave, String codigo, String comentario) {		
		this.clave = clave;
		this.codigo = codigo;
		this.comentario = comentario;
	}

	/**
	 * @return the clave
	 */
	public String getClave() {
		return clave;
	}

	/**
	 * @param clave the clave to set
	 */
	public void setClave(String clave) {
		Object old=this.clave;
		this.clave = clave;
		firePropertyChange("clave", old, clave);
	}

	/**
	 * @return the codigo
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo the codigo to set
	 */
	public void setCodigo(String codigo) {
		Object old=this.codigo;
		this.codigo = codigo;
		firePropertyChange("codigo", old, codigo);
	}

	/**
	 * @return the descripcion
	 */
	public String getComentario() {
		return comentario;
	}

	/**
	 * @param descripcion the descripcion to set
	 */
	public void setComentario(String descripcion) {
		Object old=this.comentario;
		this.comentario = descripcion;
		firePropertyChange("comentario", old, comentario);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((clave == null) ? 0 : clave.hashCode());
		result = PRIME * result + ((codigo == null) ? 0 : codigo.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Codigo other = (Codigo) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		return true;
	}
	
	public String toString(){
		return getClave()+" "+getCodigo();
	}
	

}
