package com.luxsoft.siipap.ventas.service;

import java.math.BigDecimal;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.ventas.dao.VentaDao;
import com.luxsoft.siipap.ventas.model.Venta;

public class DescuentosManagerTest extends MockObjectTestCase{
	
	private DescuentosManagerImpl manager;
	private Mock ventaDao;
	
	protected void setUp() throws Exception {
		manager=new DescuentosManagerImpl();
		ventaDao=mock(VentaDao.class);
		manager.setVentasManager((VentasManager)ventaDao.proxy());
	}
	
	public void testDescuentoVentaCamioneta(){
		Venta v=generarVenta(OrigenDeOperacion.CAM);
		
		//Fijamos la expectativa
		ventaDao.expects(once()).method("save").with(this.eq(v));
		
		//manager.actualizarDescuentos(v);
		double expected=45d;
		double actual=v.getDescuentoGeneral();
		assertEquals("Se esperaba un descuento del 45",expected, actual);
		
	}

	private Venta generarVenta(OrigenDeOperacion cam) {
		Venta v=new Venta();
		v.setImporte(BigDecimal.valueOf(35000));
		v.setDescuentoGeneral(.45);
		return v;
	}

	

}
