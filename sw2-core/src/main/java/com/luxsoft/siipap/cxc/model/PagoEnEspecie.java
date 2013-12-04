package com.luxsoft.siipap.cxc.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * Entidad de pagos mediante transferencia electónica
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("PAGO_ESP")
public class PagoEnEspecie extends Pago {

	@Override
	public String getInfo() {
		return "PAGO EN ESPECIE";
	}
	
	@Override
	public String getDepositoInfo() {
		return "PAGO EN ESPECIE";
	}

	
	public boolean isPendientesDeDeposito() {
		return false;
	}

}
