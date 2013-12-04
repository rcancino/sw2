package com.luxsoft.sw3.contabilidad.services;

import java.util.Date;

import com.luxsoft.sw3.contabilidad.model.Poliza;

/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface PolizaContableManager {
	
	public static String IVA_EN_VENTAS="IVA EN VENTAS";
	public static String IVA_EN_ANTICIPO="IVA EN ANTICIPOS DE CLIENTE";
	public static String IVA_EN_VENTAS_PENDIENTE="IVA EN VENTAS PEND. DE TRASLADAR";
	public static String IVA_EN_DEV_VTAS_PENDIENTE="IVA EN DEV. SOBRE VTAS PEND.";
	public static String IVA_EN_DESC_VTAS_PENDIENTE="IVA EN DESC. SOBRE VTAS PEND.";
	public static String IVA_EN_OTROS_INGRESOS="IVA EN OTROS INGRESOS PEND.";
	public static String IVA_EN_DEPOSITOS_IDENTIFICAR="IVA EN DEPOSITOS POR IDENTIFICAR";
	public static String IVA_POR_ACREDITAR_COMPRAS="IVA POR ACREDITAR EN COMPRAS";
	public static String IVA_POR_ACREDITAR_GASTOS="IVA POR ACREDITAR EN GASTOS";
	public static String IVA_POR_ACREDITAR_RETENIDO="IVA POR ACREDITAR RETENIDO";
	public static String IVA_RETENIDO_PENDIENTE="IVA RETENIDO PENDIENTE";
	public static String IVA_ACREDITABLE_RETENIDO="IVA ACREDITABLE RETENIDO";
	public static String IVA_RETENIDO="IVA RETENIDO";
	public static String IVA_EN_COMPRAS="IVA EN COMPRAS DE MATERIAS";
	public static String IVA_EN_GASTOS="IVA EN GASTOS";
	
	/**
	 * Genera una poliza contable sin persistir
	 * es decir con id=null
	 * 
	 * @param fecha
	 * @return
	 */
	public Poliza generarPoliza(final Date fecha);
	
	/**
	 * Salva la poiliza contable
	 * 
	 * @param poliza
	 * @return
	 */
	public Poliza salvarPoliza(final Poliza poliza);
	
	public boolean eliminarPoliza(final Poliza poliza);

}
