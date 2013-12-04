package com.luxsoft.sw3.embarque;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.ventas.model.Venta;

@Entity
@Table(name = "SX_EMBARQUE_SOLICITUDES")
public class SolicitudDeEmbarque extends BaseBean{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SOLICITUD_ID")
	private Long id;

	@Column(name = "fecha", nullable = false)
	private Date fecha = new Date();

	@Embedded
	private Direccion direccion = new Direccion();

	@Column(name = "COMENTARIO")
	@Length(max = 255, message = "El tamaño máximo permitido es de 255 ")
	private String comentario;

	@Column(name = "PARCIAL", nullable = false)
	private boolean parcial = false;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "FACTURA_ID", nullable = false, updatable = false)
	@NotNull(message="Seleccione la factura a enviar")
	private Venta factura;

	public Long getId() {
		return id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public boolean isParcial() {
		return parcial;
	}

	public void setParcial(boolean parcial) {
		this.parcial = parcial;
	}

	public Direccion getDireccion() {
		return direccion;
	}

	public void setDireccion(Direccion direccion) {
		Object old=this.direccion;
		this.direccion = direccion;
		firePropertyChange("direccion", old, direccion);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public Venta getFactura() {
		return factura;
	}

	public void setFactura(Venta factura) {
		Object old=this.factura;
		this.factura = factura;
		firePropertyChange("factura", old, factura);
		if(factura!=null){
			setDireccion(factura.getCliente().getDireccionFiscal());
		}
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
		SolicitudDeEmbarque other = (SolicitudDeEmbarque) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString() {
		return id + " " + direccion.toString();
	}

}
