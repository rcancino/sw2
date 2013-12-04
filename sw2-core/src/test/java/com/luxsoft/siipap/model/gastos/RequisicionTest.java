package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.MonedasUtils;

import junit.framework.TestCase;


public class RequisicionTest extends TestCase{
	
	/**
	 * Verifica que el total de una requisicion sea el total de  
	 * la suma de sus partidas. Para este caso solo existe una partida
	 *
	 */
	public void testTotalDeUnaRequisicionSimple(){
		CantidadMonetaria total=CantidadMonetaria.pesos(3343.15);
		Requisicion r=new Requisicion();
		r.setAfavor("TELEFONOS DE MEXICO");
		r.setFecha(new Date());
		r.setMoneda(MonedasUtils.PESOS);
		r.setOrigen(Requisicion.GASTOS);
		r.setTipoDeCambio(BigDecimal.ONE);
		int limit=1;
		for(int i=0;i<limit;i++){
			RequisicionDe det=new RequisicionDe();
			det.setDocumento("F"+i);
			det.setTotal(total);
			r.agregarPartida(det);
			det.actualizarImportesDeGastosProrrateado();
		}
		r.actualizarTotal();
		assertEquals(total, r.getTotal());
		assertEquals(MonedasUtils.calcularImporteDelTotal(total), r.getImporte());
		assertEquals(total.divide(1.15).multiply(.15), r.getImpuesto());
		assertEquals(total,r.getImporte().add(r.getImpuesto()) );
	}
	
	/**
	 * Verifica que el total de una requisicion sea el total de  
	 * la suma de sus partidas. Para el caso de n partidas
	 *
	 */
	public void testTotalDeUnaRequisicionSimpleConNPartidas(){
		CantidadMonetaria totalU=CantidadMonetaria.pesos(3343.15);
		Requisicion r=new Requisicion();
		r.setAfavor("TELEFONOS DE MEXICO");
		r.setFecha(new Date());
		r.setMoneda(MonedasUtils.PESOS);
		r.setOrigen(Requisicion.GASTOS);
		r.setTipoDeCambio(BigDecimal.ONE);
		int limit=5;
		for(int i=0;i<limit;i++){
			RequisicionDe det=new RequisicionDe();
			det.setDocumento("F"+i);
			det.setTotal(totalU);
			r.agregarPartida(det);
			det.actualizarImportesDeGastosProrrateado();
			r.actualizarTotal();
			assertEquals(totalU.multiply(i+1), r.getTotal());
		}
		CantidadMonetaria total=totalU.multiply(5);
		assertEquals(total, r.getTotal());
		assertEquals(r.getTotal(),r.getImporte().add(r.getImpuesto()) );		
		
	}
	
	public void testTotalDeReqDeGastos(){
		String prov="TELEFONOS DE MEXICO";
		Requisicion r=new Requisicion();
		r.setAfavor(prov);
		r.setFecha(new Date());
		r.setMoneda(MonedasUtils.PESOS);
		r.setOrigen(Requisicion.GASTOS);		
		r.setTipoDeCambio(BigDecimal.ONE);
		
		final CantidadMonetaria totalU=CantidadMonetaria.pesos(3342.07);
		final int rows=1;
		
		List<GFacturaPorCompra> facturas=mockFacturas(prov, rows, totalU);
		for(GFacturaPorCompra f:facturas){
			RequisicionDe det=new RequisicionDe();
			det.setFacturaDeGasto(f);
			det.setDocumento(f.getDocumento());
			det.setTotal(f.getPorRequisitar());
			r.agregarPartida(det);
			det.actualizarImportesDeGastosProrrateado();
		}
		r.actualizarTotal();
		CantidadMonetaria total=totalU.multiply(rows);
		System.out.println("Tot: "+r.getTotal());
		System.out.println("Iva: "+r.getImpuesto());
		System.out.println("Imp: "+r.getImporte());
		assertEquals(total, r.getTotal());
		assertEquals(r.getTotal(),r.getImporte().add(r.getImpuesto()) );
	}
	
	private List<GFacturaPorCompra> mockFacturas(String prov,int rows,CantidadMonetaria total){
		final List<GFacturaPorCompra> facs=new ArrayList<GFacturaPorCompra>();
		for(int i=0;i<rows;i++){
			GCompra com=mockCompra(prov, total);
			GFacturaPorCompra fac=com.crearCuentaPorPagar();
			fac.setDocumento("F"+i);
			fac.setTotal(total);			
			facs.add(fac);
			
		}
		return facs;
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
