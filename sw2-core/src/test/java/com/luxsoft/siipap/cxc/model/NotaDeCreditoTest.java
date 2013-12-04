package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;

import com.luxsoft.siipap.ventas.model.Venta;

import junit.framework.TestCase;

/**
 * Prueba la funcionalidad unitaria de las notas de credito
 * 
 * @author Ruben Cancino
 *
 */
public class NotaDeCreditoTest extends TestCase{
	
	
	public void testAplicarDescuento(){
		//La cuenta por pagar
		Venta v=new Venta();
		v.setTotal(BigDecimal.valueOf(5870.75));
		
		//La nota
		NotaDeCredito nota=new NotaDeCredito(){	
			@Override
			public String getInfo() {
				return "TEST";
			}
			
		};
		nota.setDescuento(.1);
		
		//La aplicacion
		AplicacionDeNota a=new AplicacionDeNota();
		a.setCargo(v);
		
		nota.agregarAplicacion(a);
		
		NotaRules rules=new AbstractNotasRules();
		
		rules.aplicarDescuento(nota);		
		assertEquals(587.075d, a.getImporte().doubleValue());
		
	}
	
	public void testActualizarImportesDesdeAplicaciones(){
		//La cuenta por pagar
		Venta v=new Venta();
		v.setTotal(BigDecimal.valueOf(5870.75));
		
		//La nota
		NotaDeCredito nota=new NotaDeCredito(){	
			@Override
			public String getInfo() {
				return "TEST";
			}
			
		};
		nota.setDescuento(.1);
		
		//La aplicacion
		AplicacionDeNota a=new AplicacionDeNota();
		a.setCargo(v);
		
		nota.agregarAplicacion(a);
		
		NotaRules rules=new AbstractNotasRules();
		
		rules.aplicarDescuento(nota);
		//Probar el total de la nota a partir del importe
		rules.actualizarImportesDesdeAplicaciones(nota);
		assertEquals(587.075d, nota.getTotal().doubleValue());
	}

}
