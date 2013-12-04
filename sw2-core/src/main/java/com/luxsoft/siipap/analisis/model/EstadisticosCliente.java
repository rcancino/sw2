package com.luxsoft.siipap.analisis.model;

import java.math.BigDecimal;

/**
 * Entidad para administrar los datos estadisticos del cliente
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EstadisticosCliente {
	
	private BigDecimal cargosYTD;
	private BigDecimal abonosYTD;
	
	private BigDecimal saldo;
	private BigDecimal saldoVencido;
	private BigDecimal porVencer15;
	private BigDecimal porVencer20;
	private BigDecimal porVencer25;
	private BigDecimal porVencer30;
	private BigDecimal porVencer35;
	private int chequesDevueltos;
	
	/**
	 * Veces que el cliente se atrasa en su cuenta
	 */
	private int atrasos;

}
