package com.luxsoft.siipap.cxc.ui.model;

import java.util.Map;

/**
 * Flujo de trabajo que debe implementar la UI para aplicar un pago
 * 
 * @author Ruben Cancino
 *
 */
public interface AplicacionDePagoFlow {
	
	/**
	 * Se detona al seleccionar un cliente dentro
	 * 
	 * @param context Informacion necesara para implementar un comportamiento
	 */
	public void seleccionarCliente(Map context);

}
