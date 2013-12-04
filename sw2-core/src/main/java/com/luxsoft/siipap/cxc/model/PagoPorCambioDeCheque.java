package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Entidad de pagos mediante transferencia electónica
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("PAGO_HXE")
public class PagoPorCambioDeCheque extends Pago implements Depositable{
	
	@Override
	public String getInfo() {
		return "Cambio de cheque";
	}	
	
	
	public String getOrigenAplicacion() {
		return "MOS";
	}


	public BigDecimal getCheque() {
		return getTotal();
	}


	public BigDecimal getDepositable() {
		return getTotal();
	}


	public BigDecimal getEfectivo() {
		return BigDecimal.ZERO;
	}


	public boolean isPendientesDeDeposito() {		
		return ((getDeposito()==null) && (getTotal().doubleValue()>0));
	}

}
