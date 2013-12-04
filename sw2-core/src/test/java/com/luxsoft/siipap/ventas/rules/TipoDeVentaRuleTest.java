package com.luxsoft.siipap.ventas.rules;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Comprueba el funcionamiento de la regla para la seleccion
 * de tipo de venta
 * 
 * @author Ruben Cancino
 *
 */
public class TipoDeVentaRuleTest extends TestCase{
	
	String[] tipos={"CREDITO","CONTADO"};
	
	/**
	 * Verifica los tipos si el cliente es de credito
	 */
	public void testVentaContado(){
		Cliente c=new Cliente("X044","Cliente Prueba");
		TipoDeVentaRule rule=new TipoDeVentaRule();
		Map<String, Object> context=new HashMap<String, Object>();
		rule.asignarCliente(c, new Venta(), context);
		String[] tips=(String[])context.get(VentaRule.TIPO_DE_VENTA_KEY);
		assertNotNull(tips);
		assertEquals(1, tips.length);
		assertEquals("CONTADO", tips[0]);
	}
	
	

}
