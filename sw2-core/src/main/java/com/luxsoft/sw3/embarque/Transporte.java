package com.luxsoft.sw3.embarque;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.Length;

@Entity
@Table(name="SX_TRANSPORTES")
public class Transporte implements Serializable{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="TRANSPORTE_ID")
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "CHOFER_ID", nullable = false)	
	private Chofer chofer;
	
	@Column(name="DESCRIPCION",nullable=false)
	@Length(max=255)
	private String descripcion;
	
	@Column(name="PLACAS",nullable=false)
	@Length(max=20)
	private String placas;
	
	@Column(name="POLIZA")
	@Length(max=255)
	private String poliza;

	public Chofer getChofer() {
		return chofer;
	}

	public void setChofer(Chofer chofer) {
		this.chofer = chofer;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getPlacas() {
		return placas;
	}

	public void setPlacas(String placas) {
		this.placas = placas;
	}

	public String getPoliza() {
		return poliza;
	}

	public void setPoliza(String poliza) {
		this.poliza = poliza;
	}

	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((placas == null) ? 0 : placas.hashCode());
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
		Transporte other = (Transporte) obj;
		if (placas == null) {
			if (other.placas != null)
				return false;
		} else if (!placas.equals(other.placas))
			return false;
		return true;
	}

	public String toString(){
		return this.chofer.getNombre();
	}
	
	

}
