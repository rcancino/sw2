package com.luxsoft.siipap.compras.model;

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.TestCase;

import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.DateUtil;

public class FacturaTest extends TestCase{
	
	
	public void testActualizarVencimiento(){		
		CXPFactura fac=new CXPFactura();
		fac.setFecha(DateUtil.toDate("01/07/2008"));
		try {
			fac.actualizarVencimiento();
			fail("Debio reclamar la falta de proveedor");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		}		
		Proveedor p=new Proveedor("TEST");
		p.setPlazo(30);
		fac.setProveedor(p);
		fac.actualizarVencimiento();
		Date expected=DateUtil.toDate("31/07/2008");
		assertEquals(expected, fac.getVencimiento());
	}
	
	public void testActualziarSaldo(){
		CXPFactura fac=new CXPFactura();
		fac.setTotal(BigDecimal.valueOf(55000));
		
		assertEquals(BigDecimal.valueOf(55000),fac.getSaldo());
	}

}
