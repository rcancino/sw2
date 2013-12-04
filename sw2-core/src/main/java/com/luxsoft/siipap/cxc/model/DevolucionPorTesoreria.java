package com.luxsoft.siipap.cxc.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import com.luxsoft.siipap.model.tesoreria.Requisicion;

/**
 * Cargo a la cuenta del cliente que estan relacionadas con una requisicion
 * de tesoreria
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("TES")
public class DevolucionPorTesoreria extends Cargo{

	@Override
	public String getTipoDocto() {
		return "TES";
	}
	
	@ManyToOne
	@JoinTable(name="SX_VENTAS_TESORERIA"
		,joinColumns={@JoinColumn(name="CARGO_ID")}
		,inverseJoinColumns={@JoinColumn(name="REQUISICION_ID")}
	)
	private Requisicion pago;

	public Requisicion getPago() {
		return pago;
	}

	public void setPago(Requisicion pago) {
		this.pago = pago;
	}

	
	
	
	
	

}
