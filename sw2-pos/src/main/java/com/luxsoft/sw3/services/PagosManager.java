package com.luxsoft.sw3.services;


import java.util.Date;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Manager especializado en la adminsitracion de instancias de tipo {@link Pago}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface PagosManager {
	
	
	public Pago salvar(Pago pago);
	
	
	
	/**
	 * Forma generica de buscar un abono
	 * 
	 * @param id
	 * @return
	 */
	public Abono getAbono(final String id);
	
	
	
	
	/**
	 * Cancela un pago si es posible
	 * 
	 * @param id
	 * @param aut
	 * @return
	 */
	public Pago cancelarPago(final String id,final Autorizacion2 aut,final Date fecha);
	
	/**
	 * Determina segun las reglas de negocios si un pago es cancelable
	 * 
	 * @param pago
	 * @return
	 */
	public boolean isCancelable(final Pago pago);
	
	/**
	 * Determina segun las reglas de negocios si un pago es modificable
	 * 
	 * @param pago
	 * @return
	 */
	public boolean isModificable(final Pago pago);
	
	/**
	 * Verifica q no exista un deposito con los mismos siguientes datos:
	 * 
	 * Fecha del deposito
	 * Banco emisor
	 * Cuenta destino
	 * Importe
	 * Clave del cliente
	 * 
	 */
	public boolean verificarExistenciaDeDeposito(final PagoConDeposito deposito);
	
	
	/**
	 * Cobra el saldo pendiente de la factura. Si el pago no se ha persistido lo persiste
	 * 
	 * @param venta
	 * @param pago
	 */
	public void cobrarFactura(final Venta venta,final Abono pago,final Date fecha);
	

}
