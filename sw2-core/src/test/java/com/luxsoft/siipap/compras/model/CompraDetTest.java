package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;
import java.util.Currency;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;

import junit.framework.TestCase;

public class CompraDetTest extends TestCase{
	
	
	public void testMoneda(){
		Compra c=new Compra();
		CompraDet det=new CompraDet();
		c.agregarPartida(det);
		Currency MON=MonedasUtils.PESOS;
		assertEquals(MON, det.getMoneda());
	}
	
	public void testImportePrecioNeto(){
		BigDecimal precio=BigDecimal.valueOf(550);
		Compra c=new Compra();
		CompraDet det=new CompraDet();
		c.agregarPartida(det);
		det.setSolicitado(5);
		det.setPrecio(precio);
		det.setDesc1(.1);
		//det.setDesc2(.05);
		//det.setDesc3(.03);
		CantidadMonetaria expected=CantidadMonetaria.pesos(495);
		
		//Verificar precio neto
		assertEquals(expected, det.getCosto());
		
		//Validar importe neto
		expected=CantidadMonetaria.pesos(2475);
		assertEquals(expected, det.getImporte());
	}
	
	
	
}
