package com.luxsoft.siipap.ventas.rules;

import java.util.Map;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Regla que asigna al contexto de la venta una lista de
 * posibles tipo de venta 
 * 
 * @author Ruben Cancino
 *
 */
public class TipoDeVentaRule implements VentaRule{

	public void asignarCliente(Cliente c, Venta v, Map<String, Object> context) {
		
		if(c.getCredito()!=null){
			
		}else{
			context.put(TIPO_DE_VENTA_KEY, new String[]{"CONTADO"});
		}
		
	}

	/**
	 * No interesa la asignacion de tipo de venta
	 */
	public void asignarTipoDeVenta(Venta v, Map<String, Object> context) {
		
	}

}
