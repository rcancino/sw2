package com.luxsoft.siipap.ventas.rules;

import java.util.Map;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Regla para validar la asignacion del cliente a una venta
 * 
 * @author Ruben Cancino
 *
 */
public class AsignacionDeClienteRule implements VentaRule{

	public void asignarCliente(Cliente c, Venta v, Map<String, Object> context) {
		if(v.getDocumento()!=null){
			throw new VentaRuleException("La venta ya esta facturada no se puede modificar el cliente");
		}
	}

	public void asignarTipoDeVenta(Venta v, Map<String, Object> context) {
		
	}

}
