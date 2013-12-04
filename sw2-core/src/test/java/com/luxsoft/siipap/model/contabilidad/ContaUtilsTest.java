package com.luxsoft.siipap.model.contabilidad;

import java.util.Calendar;
import java.util.Date;

import com.luxsoft.siipap.util.DateUtil;

import junit.framework.TestCase;

public class ContaUtilsTest extends TestCase{
	
	
	public void testMismoMes(){
		Date f1=DateUtil.toDate("23/01/2008");
		Date f2=DateUtil.toDate("23/02/2008");
		boolean found=ContaUtils.validarMismoMes(f1, f2);
		assertEquals(false, found);
		
		f2=DateUtil.toDate("15/01/2008");
		found=ContaUtils.validarMismoMes(f1, f2);
		assertEquals(true, found);
		
		f2=DateUtil.toDate("15/01/2007");
		found=ContaUtils.validarMismoMes(f1, f2);
		assertEquals(false, found);
		
	}
	
	public void testMismoPeriodoDelSistema(){
		Calendar c=Calendar.getInstance();
		
		c.add(Calendar.MONTH, -1);		
		boolean found=ContaUtils.validarPeriodoDelSistema(c.getTime());
		assertEquals(false, found);
		
		c.add(Calendar.YEAR, -1);		
		found=ContaUtils.validarPeriodoDelSistema(c.getTime());
		assertEquals(false, found);
		
		c.setTime(new Date());		
		found=ContaUtils.validarPeriodoDelSistema(c.getTime());
		assertEquals(true, found);
	}
	
	public void testEsMesAnterior(){
		Calendar c=Calendar.getInstance();
		c.add(Calendar.YEAR, -1);
		boolean found=ContaUtils.esMesAnterior(c.getTime());
		assertEquals(true, found);	
		
		c.add(Calendar.YEAR, +1);
		found=ContaUtils.esMesAnterior(c.getTime());
		assertEquals(false, found);
		
		c.add(Calendar.MONTH, -4);
		found=ContaUtils.esMesAnterior(c.getTime());
		assertEquals(true, found);
		
		c.add(Calendar.MONTH, +4);
		found=ContaUtils.esMesAnterior(c.getTime());
		assertEquals(false, found);
	}
	
	public void testUltimoDiaDelPeriodoAnterior(){
		
		// Un mes fijo de 31 dias
		Date f1=DateUtil.toDate("23/04/2008");
		Date expected=DateUtil.toDate("31/03/2008");
		Date found=ContaUtils.ultimoDiaDelPeriodoAnterior(f1);
		assertEquals(expected, found);
		
		// Un año bisiesto
		f1=DateUtil.toDate("23/03/2008");
		expected=DateUtil.toDate("29/02/2008");
		found=ContaUtils.ultimoDiaDelPeriodoAnterior(f1);
		assertEquals(expected, found);
		
		//Un mes de 30 dias
		f1=DateUtil.toDate("23/07/2008");
		expected=DateUtil.toDate("30/06/2008");
		found=ContaUtils.ultimoDiaDelPeriodoAnterior(f1);
		assertEquals(expected, found);
		
		//La fecha actual		
		Calendar c=Calendar.getInstance();
		c.add(Calendar.MONTH, -1);
		c.getTime();
		c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
		
		f1=DateUtil.truncate(new Date(),Calendar.DATE);
		expected=DateUtil.truncate(c.getTime(),Calendar.DATE);
		found=ContaUtils.ultimoDiaDelPeriodoAnterior(f1);		
		assertEquals(expected,found);
		
		
	}
	
	

}
