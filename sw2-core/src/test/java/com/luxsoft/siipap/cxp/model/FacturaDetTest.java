package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.util.MonedasUtils;

import junit.framework.TestCase;



/**
 * Probamos el comportamiento adecuado del bean de CXPAnalisisDet
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FacturaDetTest extends TestCase{
	
	private CXPFactura fac;
	
	public void setUp(){
		fac=new CXPFactura();
	}
	
	public void testCalcularCosto(){
		BigDecimal cantidad=BigDecimal.valueOf(48.6);
		BigDecimal precio=BigDecimal.valueOf(750.47);
		double desc1=7.5;
		double desc2=5;
		BigDecimal importe=precio.multiply(cantidad);
		importe=MonedasUtils.aplicarDescuentosEnCascada(importe, desc1,desc2);
		BigDecimal costo=importe.divide(cantidad,6,RoundingMode.HALF_EVEN);
		
		System.out.println("Importe esperado: "+importe);
		System.out.println("Costo esperado  : "+costo+ " Calculado: "+costo.doubleValue()*cantidad.doubleValue());
		
		
		CXPAnalisisDet det=new CXPAnalisisDet();
		det.setCantidad(cantidad.doubleValue());
		det.setPrecio(precio);
		det.setDesc1(desc1);
		det.setDesc2(desc2);
		det.calcularImporte();
		
		assertEquals(importe, det.getImporte());
		System.out.println("Importe encontrado: "+det.getImporte());
		
		assertEquals(costo, det.getCosto());
		System.out.println("Costo encontrado: "+det.getCosto());
	}
	
	public void testTransferirCostoAInventario(){
		BigDecimal cantidad=BigDecimal.valueOf(48.6);
		BigDecimal precio=BigDecimal.valueOf(750.47);
		double desc1=7.5;
		double desc2=5;
		
		EntradaPorCompra e=new EntradaPorCompra();
		e.setCantidad(cantidad.doubleValue());
		
		CXPAnalisisDet det=new CXPAnalisisDet();
		fac.agregarPartida(det);
		det.setCantidad(cantidad.doubleValue());
		det.setEntrada(e);
		det.setPrecio(precio);
		det.setDesc1(desc1);
		det.setDesc2(desc2);
		det.calcularImporte();
		det.actualizarInventario();
		
		assertEquals(det.getCosto(), e.getCosto());
		System.out.println("\nCosto en Analisis: "+det.getCosto()+ " Costo en Inv: "+e.getCosto());
	}

}
