package com.luxsoft.siipap.cxc.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.luxsoft.siipap.model.Autorizacion2;



/**
 * Autorizacion generica para cargos
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("PARA_CARGO")
public class AutorizacionParaCargo extends Autorizacion2{
	
	@ManyToOne
	@JoinColumn(name = "CARGO_ID")	
	private Cargo cargo;

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}
	
	

}
