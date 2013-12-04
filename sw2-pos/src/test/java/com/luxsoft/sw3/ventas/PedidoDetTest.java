package com.luxsoft.sw3.ventas;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

public class PedidoDetTest {

	/**
	 * Verifica el calculo de los importes
	 * 
	 */
	@Test
	public void testActualizar() {
		
		PedidoDet partida=PedidoDet.getPedidoDet();
		
		partida.setPrecio(BigDecimal.valueOf(600.00));
		partida.setCantidad(3000.00);
		partida.setFactor(1000.00);
		partida.setDescuento(48);
		partida.setCortes(5);
		partida.setPrecioCorte(BigDecimal.valueOf(10));
		partida.actualizar();
		
		BigDecimal impBruto=BigDecimal.valueOf(1800.00);
		assertEquals(impBruto.doubleValue(),partida.getImporteBruto().doubleValue(),.0001d);
		System.out.println("Importe Bruto ok: "+partida.getImporteBruto());
		
		BigDecimal impDescuento=BigDecimal.valueOf(864.00);
		assertEquals(impDescuento.doubleValue(), partida.getImporteDescuento().doubleValue(),.0001d);
		System.out.println("Importe Descto ok: "+partida.getImporteDescuento());
		
		BigDecimal neto=BigDecimal.valueOf(936.00);
		assertEquals(neto.doubleValue(), partida.getImporteNeto().doubleValue(),.0001d);
		System.out.println("Importe Neto ok: "+partida.getImporteNeto());
		
		BigDecimal impCortes=BigDecimal.valueOf(50.00);
		assertEquals(impCortes.doubleValue(), partida.getImporteCorte().doubleValue(),.0001d);
		System.out.println("Importe Cortes ok: "+partida.getImporteCorte());
		
		BigDecimal subtotal=BigDecimal.valueOf(986.00);
		assertEquals(subtotal.doubleValue(), partida.getSubTotal().doubleValue(),.0001d);
		System.out.println("SubTotal ok: "+partida.getSubTotal());
	}

}
