package com.luxsoft.siipap.cxc.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Cargo relacionado con una devolucion en efectivo/cheque
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("TES")
public class CargoPorTesoreria extends Cargo{
	
	

	@Override
	public String getTipoDocto() {
		return "TES";
	}
	
	
	
	
}
