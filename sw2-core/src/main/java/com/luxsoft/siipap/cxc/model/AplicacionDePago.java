package com.luxsoft.siipap.cxc.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * Aplicacion de pago especifica para instancias de {@link Pago} 
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("PAGO")
public class AplicacionDePago extends Aplicacion{

	@Override
	public String getTipo() {
		return "APL PAGO";
	}
	
	public Pago getPago(){
		return (Pago)getAbono();
	}

}
