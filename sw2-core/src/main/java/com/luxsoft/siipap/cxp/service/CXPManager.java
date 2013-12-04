package com.luxsoft.siipap.cxp.service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import com.luxsoft.siipap.model.core.Proveedor;

/**
 * Manager centralizado de servicios relacionados con
 * CxP
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface CXPManager {
	
	/**
	 * Regresa el saldo de un proveedor a la fecha(no inclusiva) indicado
	 *  Es decir incluye movimientos anteriores a la fecha indicada
	 *  
	 * @param p
	 * @param fecha La fecha de corte, No inclusiva.
	 * @return
	 */
	public BigDecimal getSaldo(final Proveedor p,final Date fecha);
	
	/**
	 * 
	 * Regresa el saldo de un proveedor a la fecha(no inclusiva) indicado en la moneda indicada
	 * 
	 *  Es decir incluye movimientos anteriores a la fecha indicada
	 *  
	 * @param p
	 * @param moneda
	 * @param fecha
	 * @return
	 */
	public BigDecimal getSaldo(final Proveedor p,Currency  moneda,final Date fecha);

}
