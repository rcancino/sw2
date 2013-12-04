package com.luxsoft.siipap.ventas.rules;

import java.util.HashMap;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

import junit.framework.TestCase;

/**
 * Prueba el buen funcionamiento de la regla de validacion
 * para la asignacion de un cliente a una venta 
 *  
 * @author Ruben Cancino
 *
 */
public class AsignacionDeclienteRuleTest extends TestCase{
	
	public void testErrorOnVentaFacturada(){
		Venta v=new Venta();
		v.setDocumento(new Long(24233));
		VentaRule rule=new AsignacionDeClienteRule();
		Cliente c=new Cliente("X044","Cliente Prueba");
		try{
			rule.asignarCliente(c, v, new HashMap<String, Object>());
			fail("Regla invalida debe mandar error");
		}catch(VentaRuleException ve){
			assertNotNull(ve);
		}
	}

}
