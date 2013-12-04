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
@DiscriminatorValue("PAGO_EFE")
public class PagoConEfectivo extends Pago implements Depositable{

	@Override
	public String getInfo() {
		return "EFECTIVO";
	}
	
	@Override
	public String getDepositoInfo() {
		if(getDeposito()!=null)
			return getDeposito().toString();
		return "PENDIENTE";
	}

	public BigDecimal getCheque() {
		return BigDecimal.ZERO;
	}

	public BigDecimal getDepositable() {
		return getTotal();
	}

	public BigDecimal getEfectivo() {
		return getTotal();
	}

	public boolean isPendientesDeDeposito() {
		return (getDeposito()==null && getTotal().doubleValue()>0);
	}
	
	@Override
	public String getAutorizacionInfo() {
		return "AUTORIZADO";
	}
	

}
