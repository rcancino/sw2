package com.luxsoft.siipap.cxc.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("NOTA")
public class AplicacionDeNota extends Aplicacion{
	
	/**
	 * Util solo para la migracion de informacion
	 */
	@Transient
	public int reglonSiipap;
	
	

	@Override
	public String getTipo() {
		return "APL NOTA";
	}
	
	public NotaDeCredito getNota(){
		return (NotaDeCredito)getAbono();
	}
	
		
	

}
