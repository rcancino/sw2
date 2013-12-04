package com.luxsoft.sw3.embarque;

import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;

/**
 * JavaBean para representar un comentario
 * 
 * @author Ruben Cancino
 *
 */
@Embeddable
public class ChoferObservacion extends BaseBean{
	
	@NotNull 	
	private Date fecha;
	
	@NotNull 
	@Length(max=255)
	@Column(name="OBSERVACION")
	private String observacion;
	
	
	public ChoferObservacion() {
		
	}

	public ChoferObservacion(Date fecha, String observacion) {		
		this.fecha = fecha;
		this.observacion = observacion;
	}

	

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;		
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}
	
	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		Object old=this.observacion;
		this.observacion = observacion;
		firePropertyChange("observacion", old, observacion);
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result
				+ ((observacion == null) ? 0 : observacion.hashCode());
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
		ChoferObservacion other = (ChoferObservacion) obj;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (observacion == null) {
			if (other.observacion != null)
				return false;
		} else if (!observacion.equals(other.observacion))
			return false;
		return true;
	}

	public String toString(){
		return MessageFormat.format("{0,date,short} {1}",getFecha(),getObservacion());
	}

}
