package com.luxsoft.siipap.compras.model;

import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;

import junit.framework.TestCase;

public class ListaDePreciosDetTest extends TestCase{
	
	/**
	 * Pruieba que los descuentos se almacenen correctamente
	 * , se puedan repetidos y el orden sea el natural a una lista
	 * 
	 */
	public void testDescuentos(){
		ListaDePreciosDet det=new ListaDePreciosDet();
		det.setProducto(new Producto("1"));
		det.setPrecio(CantidadMonetaria.pesos(450));
		det.setDescuento1(10);
		det.setDescuento2(10);
		assertEquals(CantidadMonetaria.pesos(364.5), det.getCosto());
	}
	
	public void testCalcularCosto(){
		ListaDePreciosDet det=new ListaDePreciosDet();
		det.setProducto(new Producto("1"));
		det.setPrecio(CantidadMonetaria.pesos(500));
		det.setDescuento1(10);
		CantidadMonetaria expected=CantidadMonetaria.pesos(450);
		CantidadMonetaria found=det.getCosto();
		assertEquals(expected, found);
		
		det.setDescuento2(30);
		expected=CantidadMonetaria.pesos(315);
		found=det.getCosto();
		assertEquals(expected, found);
	}
	
	

}
