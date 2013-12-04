package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.util.List;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.util.MonedasUtils;

import junit.framework.TestCase;

public class RequisicionesUtilsTest extends TestCase{
	
	public void testGenerarRequisicionDeFacturaDeGasto(){
		
		CantidadMonetaria total=CantidadMonetaria.pesos(3342.07);
		CantidadMonetaria importe=MonedasUtils.calcularImporteDelTotal(total);
		CantidadMonetaria impuesto=importe.multiply(.15);
		assertEquals(total, importe.add(impuesto));
		
		//Compra origen
		
		GCompra com=mockCompra("TELEFONOS DE MEXICO", total);
		assertEquals(importe, com.getImporteEnCantidadMonetaria());
		assertEquals(impuesto, com.getImpuestoEnCantidadMonetaria());
		assertEquals(total, com.getTotalAsCantidadMonetaria());
		
		//CXPFactura de compra
		GFacturaPorCompra fac=com.crearCuentaPorPagar();
		assertEquals(importe, fac.getImporte());
		assertEquals(impuesto, fac.getImpuesto());
		assertEquals(total, fac.getTotal());
		
		Requisicion r=RequisicionesUtils.generarRequisicion(fac);
		assertEquals(importe, r.getImporte());
		assertEquals(impuesto, r.getImpuesto());
		assertEquals(total, r.getTotal());
		
		
	}
	
	
	private GCompra mockCompra(String prov,CantidadMonetaria total){
		GCompra c=new GCompra();
		c.setProveedor(new GProveedor(prov));
		GCompraDet det=new GCompraDet();
		det.setCantidad(BigDecimal.ONE);
		det.setPrecio(total.divide(1.15).amount());
		c.agregarPartida(det);
		//c.actualizar();
		//c.actualizarSaldoDeFacturas();
		c.actualizarTotal();
		return c;
	}
	

}
