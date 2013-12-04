package com.luxsoft.siipap.model.gastos;

import java.util.Date;

import com.luxsoft.siipap.util.DateUtil;

import junit.framework.TestCase;

public class GFacturaPorCompraTest extends TestCase{
	
	public void testFechaFactura(){
		Date expected=DateUtil.toDate("30/06/2008");
		GFacturaPorCompra fac=new GFacturaPorCompra();
		fac.setFecha(expected);
		fac.acutlizarFechaContable();
		assertEquals(expected, fac.getFechaContable());
		
	}

}
