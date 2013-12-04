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
@DiscriminatorValue("PAGO_TXE")
public class PagoPorCambioDeTarjeta extends PagoConTarjeta {
	
	@Override
	public String getInfo() {
		return "Cambio de tarjeta";
	}	
	
	
	public String getOrigenAplicacion() {
		return "MOS";
	}


	public boolean isPendientesDeDeposito() {		
		return ((getDeposito()==null) && (getTotal().doubleValue()>0));
	}

}
