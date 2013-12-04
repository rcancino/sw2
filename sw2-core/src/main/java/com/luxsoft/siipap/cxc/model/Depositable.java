package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;

public interface Depositable {
	
	/**
	 * El importe depositable
	 * 
	 * @return
	 */
	public BigDecimal getDepositable();
	
	/**
	 * Importe en cheque a depositar
	 * 
	 * @return
	 */
	public BigDecimal getCheque();
	
	/**
	 * Importe en efectivo a depositar
	 * 
	 * @return
	 */
	public BigDecimal getEfectivo();
	
	
	/**
	 * Referencia al detalle de la ficha de  deposito
	 * 
	 * @return
	 */
	public FichaDet getDeposito();
	
	/**
	 * Indica si el importe esta pendiente de deposito 
	 * 
	 * @return
	 */
	public boolean isPendientesDeDeposito();
	
	/**
	 * El nombre del banco origen del cheque  
	 * 
	 * @return
	 */
	public String getBanco();
	
	
	

}
