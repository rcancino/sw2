package com.luxsoft.siipap.compras.model;

import java.util.Date;

import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.DateUtil;

import junit.framework.TestCase;

public class ListaDePreciosTest extends TestCase{
	
	public void testPreiodo(){
		
		Date d1=DateUtil.toDate("01/07/2008");
		Date d2=DateUtil.toDate("01/06/2008");
		
		try {
			new ListaDePrecios(d1,d2);
			fail("Debio mandar error ya q el periodo es incorrecto");
		} catch (Exception e) {
			assertNotNull(e);
		}
		
		d2=DateUtil.toDate("31/07/2008");
		ListaDePrecios l=new ListaDePrecios(d1,d2);
		System.out.println("Lista : "+l);
	}
	
	public void testPartidas(){
		ListaDePreciosDet det=new ListaDePreciosDet();
		det.setProducto(new Producto(""));
		det.setPrecio(CantidadMonetaria.pesos(500));
		
		ListaDePreciosDet det2=new ListaDePreciosDet();
		det2.setProducto(new Producto(""));
		det2.setPrecio(CantidadMonetaria.pesos(500));
		
		// Producto repetido
		ListaDePrecios l=new ListaDePrecios();
		l.getPrecios().add(det);
		l.getPrecios().add(det2);
		assertEquals(1, l.getPrecios().size());
		
		//Producto ok
		det2.setProducto(new Producto("2"));
		l.getPrecios().add(det2);
		assertEquals(2, l.getPrecios().size());
	}
	
	public void testLocalizarPrecio(){
		ListaDePreciosDet det=new ListaDePreciosDet();
		det.setProducto(new Producto("P1"));
		det.setPrecio(CantidadMonetaria.pesos(475));
		
		ListaDePreciosDet det2=new ListaDePreciosDet();
		det2.setProducto(new Producto("P2"));
		det2.setPrecio(CantidadMonetaria.pesos(500));
		
		
		ListaDePrecios l=new ListaDePrecios();
		l.getPrecios().add(det);
		l.getPrecios().add(det2);
		
		Producto target=new Producto("PX");
		ListaDePreciosDet found=l.buscarPrecio(target);
		
		assertNull("No debio encontrar el producto",found);
		
		Producto target2=new Producto("P1");
		found=l.buscarPrecio(target2);
		assertNotNull(found);
		
		/// Verificar que el precio sea el correcto 
		CantidadMonetaria expected=CantidadMonetaria.pesos(475);		
		assertEquals(expected, found.getPrecio());
		
		
	}

}
