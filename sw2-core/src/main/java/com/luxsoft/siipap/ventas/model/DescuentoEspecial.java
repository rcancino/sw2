package com.luxsoft.siipap.ventas.model;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.core.Descuento;

/**
 * Descuento especial para un Cargo
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_VENTAS_DESCUENTOS")
public class DescuentoEspecial {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "CARGO_ID", nullable = false, updatable = false,unique=true)
	private Cargo cargo;
	
	
	@ManyToOne(optional = false,cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinColumn(name = "AUT_ID", nullable = false)
	private Autorizacion2 autorizacion;
	
	@Embedded
	private Descuento descuento;

	public Long getId() {
		return id;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public Descuento getDescuento() {
		return descuento;
	}

	public void setDescuento(Descuento descuento) {
		this.descuento = descuento;
	}

	public Autorizacion2 getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(Autorizacion2 autorizacion) {
		this.autorizacion = autorizacion;
	}	

	
	

}
