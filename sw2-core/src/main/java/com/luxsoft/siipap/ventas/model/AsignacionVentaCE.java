package com.luxsoft.siipap.ventas.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

@Entity
@Table(name="SX_ASIGNACION_CE")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class AsignacionVentaCE {

	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="ID")
	private String id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "VENTA_ID", nullable = false)
	private Venta venta;
	
	@Column(name = "ASIGNACION", nullable = false)
	@Type(type = "date")
	private Date asignacion = new Date();
	
	@Column(name = "FECHA_FAC", nullable = false)
	@Type(type = "date")
	private Date fechaFactura = new Date();
	
	@Length (max=250,message="El tamaño máximo del comentario es de 250 caracteres")
	private String comentario;
	
	@Length (max=50,message="El tamaño máximo del comentario es de 50 caracteres")
	private String solicito;
	
	

	public AsignacionVentaCE() {
		
	}
	
	public AsignacionVentaCE(Venta venta) {
		setVenta(venta);
		setFechaFactura(venta.getFecha());
		setAsignacion(venta.getFecha());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		this.venta = venta;
	}

	public Date getAsignacion() {
		return asignacion;
	}

	public void setAsignacion(Date asignacion) {
		this.asignacion = asignacion;
	}

	public Date getFechaFactura() {
		return fechaFactura;
	}

	public void setFechaFactura(Date fechaFactura) {
		this.fechaFactura = fechaFactura;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}	

	public String getSolicito() {
		return solicito;
	}

	public void setSolicito(String solicito) {
		this.solicito = solicito;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((venta == null) ? 0 : venta.hashCode());
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
		AsignacionVentaCE other = (AsignacionVentaCE) obj;
		if (venta == null) {
			if (other.venta != null)
				return false;
		} else if (!venta.equals(other.venta))
			return false;
		return true;
	}
	
	
	
	
}
