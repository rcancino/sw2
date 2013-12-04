package com.luxsoft.sw3.embarque;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

/**
 * Reporte de insidentes posibles en el transcurso de un embarque
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Embeddable
public class Incidente {
	
	@Type (type="timestamp")
	@Column(name="HORA",updatable=false)	
	private Date hora;
	
	@Column(name="COMENTARIO",nullable=false)
	@Length(max=255, message="El tamaño maximo del comentario es de 255 caracteres")
	@NotNull
	private String comentario;
	
	@Column(name="COMENTARIO2")
	@Length(max=255, message="El tamaño maximo del comentario es de 255 caracteres")
	private String comentario2;
	
	public Incidente(){
	}

	public Incidente(String comentario) {		
		this.comentario = comentario;
		this.hora=new Date();
	}

	public Date getHora() {
		return hora;
	}

	public void setHora(Date hora) {
		this.hora = hora;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		this.comentario2 = comentario2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hora == null) ? 0 : hora.hashCode());
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
		Incidente other = (Incidente) obj;
		if (hora == null) {
			if (other.hora != null)
				return false;
		} else if (!hora.equals(other.hora))
			return false;
		return true;
	}
	
	

}
