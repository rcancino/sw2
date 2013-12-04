package com.luxsoft.sw3.embarque;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Entidad par representar zonas de envio con cargo  
 *  
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_EMBARQUES_ZONAS_CARGO"
		,uniqueConstraints=@UniqueConstraint(columnNames={"ESTADO","CIUDAD"})
		)
public class ZonaDeEnvio {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ZONA_ID")
	private Long id;
	
	@Column(name="ESTADO",nullable=false)
	private String estado;
	
	@Column(name="CIUDAD",nullable=false)
	private String ciudad;
	
	@Column(name="TARIFA",nullable=false)
	private BigDecimal tarifa;
	
	@Column(name="MULTIPLO",nullable=false)
	private int multiplo=3;
	
	@Column(name="UNIDAD",nullable=false)
	private String unidad;

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public BigDecimal getTarifa() {
		return tarifa;
	}

	public void setTarifa(BigDecimal tarifa) {
		this.tarifa = tarifa;
	}

	public int getMultiplo() {
		return multiplo;
	}

	public void setMultiplo(int multiplo) {
		this.multiplo = multiplo;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ciudad == null) ? 0 : ciudad.hashCode());
		result = prime * result + ((estado == null) ? 0 : estado.hashCode());
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
		ZonaDeEnvio other = (ZonaDeEnvio) obj;
		if (ciudad == null) {
			if (other.ciudad != null)
				return false;
		} else if (!ciudad.equals(other.ciudad))
			return false;
		if (estado == null) {
			if (other.estado != null)
				return false;
		} else if (!estado.equals(other.estado))
			return false;
		return true;
	}
	
	

}
