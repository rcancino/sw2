package com.luxsoft.siipap.gastos.operaciones;

import java.math.BigDecimal;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.gastos.RequisicionesUtils;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.util.MonedasUtils;

import junit.framework.TestCase;

public class RequisicionAutomaticaModelTest extends TestCase{
	
	public void testImportes(){
		
		CantidadMonetaria total=CantidadMonetaria.pesos(3342.07);
		CantidadMonetaria importe=MonedasUtils.calcularImporteDelTotal(total);
		CantidadMonetaria impuesto=importe.multiply(.15);
		assertEquals(total, importe.add(impuesto));
		
		//Compra origen		
		GCompra com=mockCompra("TELEFONOS DE MEXICO", total);		
		//CXPFactura de compra
		GFacturaPorCompra fac=com.crearCuentaPorPagar();
		
		Requisicion r=RequisicionesUtils.generarRequisicion(fac);
		assertEquals(importe, r.getImporte());
		assertEquals(impuesto, r.getImpuesto());
		assertEquals(total, r.getTotal());
		
		RequisicionAutomaticaModel model=new RequisicionAutomaticaModel(r);
		r.actualizarTotal();
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
