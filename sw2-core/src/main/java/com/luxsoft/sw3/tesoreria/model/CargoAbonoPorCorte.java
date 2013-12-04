package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Parent;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;

@Embeddable
public class CargoAbonoPorCorte implements Comparable<CargoAbonoPorCorte>{
	
	@ManyToOne (optional=true,fetch=FetchType.LAZY)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)
	@NotNull
	private Sucursal sucursal;
	
	@Parent
	private CorteDeTarjeta corte;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO", nullable = false, length = 20)
	private TipoDeAplicacion tipo;
		
	
	@ManyToOne(optional = false
			,fetch=FetchType.EAGER
			,cascade={
			CascadeType.MERGE
			,CascadeType.PERSIST
			,CascadeType.REMOVE}
			)			
	@JoinColumn(name = "CARGOABONO_ID", nullable = false)
	private CargoAbono cargoAbono;
	
	@Column (name="IMPORTE",nullable=false,scale=2,precision=16)
	private BigDecimal importe=BigDecimal.ZERO;	
		
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Column(name="ORDEN",nullable=false)
	public int orden;

	public CorteDeTarjeta getCorte() {
		return corte;
	}

	public void setCorte(CorteDeTarjeta corte) {
		this.corte = corte;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public TipoDeAplicacion getTipo() {
		return tipo;
	}

	public void setTipo(TipoDeAplicacion tipo) {
		this.tipo = tipo;
	}

	

	public CargoAbono getCargoAbono() {
		return cargoAbono;
	}

	public void setCargoAbono(CargoAbono ingreso) {
		this.cargoAbono = ingreso;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}


	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cargoAbono == null) ? 0 : cargoAbono.hashCode());
		result = prime * result + ((corte == null) ? 0 : corte.hashCode());
		result = prime * result + orden;
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
		CargoAbonoPorCorte other = (CargoAbonoPorCorte) obj;
		if (cargoAbono == null) {
			if (other.cargoAbono != null)
				return false;
		} else if (!cargoAbono.equals(other.cargoAbono))
			return false;
		if (corte == null) {
			if (other.corte != null)
				return false;
		} else if (!corte.equals(other.corte))
			return false;
		if (orden != other.orden)
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}

	public int compareTo(CargoAbonoPorCorte o) {
		if(o!=null){
			return this.orden>o.getOrden()?1:-1;
		}
		return 0;
	}

	
	

}
