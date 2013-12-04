package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.ComprasFactory;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.MonedasUtils;

import junit.framework.TestCase;

public class EntradaPorCompraTest extends TestCase{
	
	
	public void testCostoHeredadoPesos(){
		Compra c=new Compra();		
		CompraDet det=new CompraDet();
		c.agregarPartida(det);
		det.setSolicitado(50);
		det.setPrecio(BigDecimal.valueOf(750));
		det.setProducto(new Producto(""));
		EntradaPorCompra e=ComprasFactory.crearEntrada(det);
		assertEquals(CantidadMonetaria.pesos(0).amount().doubleValue(), e.getCostoPromedio().doubleValue());
		assertEquals(CantidadMonetaria.pesos(750).amount(), e.getCostoUltimo());
		//assertEquals(CantidadMonetaria.pesos(750).amount(), e.getCosto());
	}
	
	public void testCostoHeredadoDolares(){
		Compra c=new Compra();
		c.setMoneda(MonedasUtils.DOLARES);
		c.setTc(BigDecimal.valueOf(10.45));
		CompraDet det=new CompraDet();
		c.agregarPartida(det);
		det.setSolicitado(50);
		det.setPrecio(BigDecimal.valueOf(35));
		det.setProducto(new Producto(""));
		EntradaPorCompra e=ComprasFactory.crearEntrada(det);
		
		assertEquals(CantidadMonetaria.pesos(0).amount().doubleValue(), e.getCostoPromedio().doubleValue());
		assertEquals(CantidadMonetaria.pesos(365.75).amount(), e.getCostoUltimo());
		assertEquals(CantidadMonetaria.pesos(365.75).amount(), e.getCosto());
	}
	
	public void testAnalizado(){
		Compra c=new Compra();
		c.setMoneda(MonedasUtils.DOLARES);
		c.setTc(BigDecimal.valueOf(10.45));
		CompraDet det=new CompraDet();
		c.agregarPartida(det);
		det.setSolicitado(50);
		det.setPrecio(BigDecimal.valueOf(35));
		det.setProducto(new Producto(""));
		EntradaPorCompra e=ComprasFactory.crearEntrada(det);
		
		CXPAnalisisDet facdet=new CXPAnalisisDet();
		facdet.setCantidad(25);
		//e.getFacturas().add(facdet);
		
		assertEquals(50d, e.getCantidad());
		
		assertEquals(25d, e.getAnalizado());
		
	}
	
	public void testPorAnalizar(){
		Compra c=new Compra();
		c.setMoneda(MonedasUtils.DOLARES);
		c.setTc(BigDecimal.valueOf(10.45));
		CompraDet det=new CompraDet();
		c.agregarPartida(det);
		det.setSolicitado(50);
		det.setPrecio(BigDecimal.valueOf(35));
		det.setProducto(new Producto(""));
		EntradaPorCompra e=ComprasFactory.crearEntrada(det);
		
		CXPAnalisisDet facdet=new CXPAnalisisDet();
		facdet.setCantidad(25);
		//e.getFacturas().add(facdet);
		
		assertEquals(50d, e.getCantidad());
		
		//assertEquals(25d, e.getPorAnalizar());
	}

}
