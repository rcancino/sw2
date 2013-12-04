package com.luxsoft.siipap.cxc.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Pruebas unitarias para el funcionamiento
 * de los descuentos
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class NotaDescuentoRulesTest extends TestCase{
	
	
	public void testCalcularImporteDeDescuentoPrecioNeto(){
		Venta v=new Venta();
		v.setDescuentoGeneral(.50);
		v.setPrecioBruto(false);
		v.setTotal(BigDecimal.valueOf(100000));		
		v.setDevoluciones(BigDecimal.valueOf(25000));
		
		NotaDescuentoRules rule=new NotaDescuentoRules();
		BigDecimal res=rule.calcularImporteDeDescuento(v);
		
		assertEquals("Los cargos a precio neto no pueden pueden obtener un descuento por nota de credito"
				,BigDecimal.ZERO, res);
		
		
	}
	
	/***
	 * Verifica  el importe de la nota cuando el cargo 
	 * es precio bruto
	 * 
	 */
	public void testCalcularImporteDeDescuentoPrecioBruto(){
		
		Venta v=new Venta();
		v.setPrecioBruto(true);
		v.setTotal(BigDecimal.valueOf(200000));		
		v.setDevoluciones(BigDecimal.valueOf(50000));
		v.setBonificaciones(BigDecimal.valueOf(5000));
		v.setDescuentoGeneral(.50);
		NotaDescuentoRules rule=new NotaDescuentoRules();
		BigDecimal res=rule.calcularImporteDeDescuento(v);
		BigDecimal expected=BigDecimal.valueOf(72500.00);
		assertEquals(expected.doubleValue(), res.doubleValue());

		//Cuando el descuento esta aplicado
		v.setDescuentos(expected);
		res=rule.calcularImporteDeDescuento(v);
		assertEquals("El cargo no puede tener descuentos aplicados"
				,BigDecimal.ZERO, res);
		
	}
	
	/**
	 * Prueba la correcta generacion de una nota de credito a partir
	 * de una lista de aplicaciones
	 * 
	 */
	public void testCrearNota(){
		List<AplicacionDeNota> aplicaciones=new ArrayList<AplicacionDeNota>();
		NotaDescuentoRules rule=new NotaDescuentoRules();
		try {
			rule.crearNotaDeCredito(aplicaciones);
			fail("Debe mandar error cuando la lista de aplicaciones este vacia");
		} catch (Exception e) {}
		
		aplicaciones=mockAplicaiones(15,BigDecimal.valueOf(1000));
		NotaDeCreditoDescuento nota=rule.crearNotaDeCredito(aplicaciones);
		
		assertTrue("Solo debe tomar en cuenta 10 aplicaciones",nota.getAplicaciones().size()==10); 
		assertEquals("El total de la nota debe ser la suma de las aplicaciones",10000d, nota.getTotal().doubleValue());
		
		
	}
	
	/**
	 * Prueba que se generen el numero adecuado de notas de credito a partir
	 * de un numero x de aplicaciones
	 */
	public void testGenerarNotas(){
		NotaDescuentoRules rule=new NotaDescuentoRules();
		List<AplicacionDeNota> aplicaciones=mockAplicaiones(35,BigDecimal.valueOf(1000));
		List<NotaDeCreditoDescuento> notas=rule.generarNotas(aplicaciones);
		assertEquals("Debio generar 4 notas de credito por descuento para 35 aplicaciones",4, notas.size());
		
	}
	
	
	private List<AplicacionDeNota> mockAplicaiones(int rows,BigDecimal importe){
		Cliente c=new Cliente("TEST","TEST");
		List<AplicacionDeNota> res=new ArrayList<AplicacionDeNota>();
		for(int index=0;index<rows;index++){
			Venta v=new Venta();
			v.setCliente(c);			
			v.setDescuentoGeneral(.5);
			AplicacionDeNota a=new AplicacionDeNota();
			a.setCargo(v);
			a.setImporte(importe);
			res.add(a);
		}
		
		return res;
	}

}
