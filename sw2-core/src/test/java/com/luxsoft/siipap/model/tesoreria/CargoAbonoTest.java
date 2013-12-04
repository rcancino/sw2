package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.TestCase;

import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.DateUtil;

public class CargoAbonoTest extends TestCase{
	
	
	public void testEncyptOn(){
		Cuenta cta=getCuenta();
		cta.setEncriptar(true);
		BigDecimal importe =BigDecimal.valueOf(555);
		Date fecha=DateUtil.toDate("01/02/2008");
		Sucursal suc=new Sucursal();
		CargoAbono ca=CargoAbono.crearAbono(cta, importe, fecha, null,suc);
		assertFalse(ca.getImporte().equals(BigDecimal.valueOf(555)));
		System.out.println(ca.getImporte());
	}
	
	public void testEncyptOff(){
		Cuenta cta=getCuenta();
		cta.setEncriptar(false);
		BigDecimal importe =BigDecimal.valueOf(555);
		Date fecha=DateUtil.toDate("01/02/2008");
		Sucursal suc=new Sucursal();
		CargoAbono ca=CargoAbono.crearAbono(cta, importe, fecha, null,suc);
		assertEquals(BigDecimal.valueOf(555),ca.getImporte());
		System.out.println(ca.getImporte());
	}
	
	public void testDecrypt(){
		Cuenta cta=getCuenta();
		cta.setEncriptar(true);
		BigDecimal importe =BigDecimal.valueOf(555);
		Date fecha=DateUtil.toDate("01/02/2008");
		Sucursal suc=new Sucursal();
		CargoAbono ca=CargoAbono.crearAbono(cta, importe, fecha, null,suc);
		assertFalse(ca.getImporte().equals(BigDecimal.valueOf(555)));
		System.out.println(ca.getImporte());
		
		assertEquals(BigDecimal.valueOf(555),ca.getImporte());
		System.out.println("Importe decriptado: "+ca.getImporte());
	}
	
	private Cuenta getCuenta(){
		Empresa e=new Empresa("E1","Empres Test");
		Banco b=new Banco("B1","TestBanco");
		b.setEmpresa(e);
		Cuenta cta=new Cuenta(b);
		return cta;
	}

}
