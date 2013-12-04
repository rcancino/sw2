package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;

import junit.framework.TestCase;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.ComprasFactory;
import com.luxsoft.siipap.model.CantidadMonetaria;

public class CompraTest extends TestCase{
	
	public void testImporte(){
		
		Compra c=new Compra();		
		double c1=50;
		double p1=250;		
		double c2=60;
		double p2=350;
		
		c.agregarPartida(getPartida(c1, p1, c));
		c.agregarPartida(getPartida(c2, p2, c));
		
		assertEquals(2,c.getPartidas().size() );
		
		CantidadMonetaria expected=CantidadMonetaria.pesos(33500);
		assertEquals(expected, c.getImporte());
		
	}
	
	private CompraDet getPartida(final double cantidad,double precio,Compra compra){
		CompraDet det=ComprasFactory.crearPartida(compra);
		det.setPrecio(BigDecimal.valueOf(precio));
		det.setSolicitado(cantidad);
		return det;
	}

}
