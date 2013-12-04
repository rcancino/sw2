package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.util.DateUtil;

import junit.framework.TestCase;

public class ActivoFijoTest extends TestCase{
	
	
	public void testMesesDeUso(){
		final Date fechaAdq=DateUtil.toDate("3/03/2004");
		final Date fechaAct=DateUtil.toDate("31/12/2007");
		final ActivoFijo af=new ActivoFijo();
		af.setFechaDeAdquisicion(fechaAdq);
		af.setFechaActualizacion(fechaAct);
		int expected=46;
		assertEquals(expected, af.getMesesUso());
		
	}
	
	public void testDepreciacionInicial(){		
		final Date fechaAdq=DateUtil.toDate("25/07/2003");
		final Date fechaAct=DateUtil.toDate("31/12/2006");
		final ActivoFijo af=new ActivoFijo();
		af.setFechaDeAdquisicion(fechaAdq);
		af.setFechaActualizacion(fechaAct);
		af.setMoi(BigDecimal.valueOf(841280.00));
		af.setTasaDepreciacion(10.00);
		BigDecimal expected=BigDecimal.valueOf(203309.43);
		assertEquals(expected, af.getDepreciacionInicial());
		
	}
	
	public void testFactorDeActualizacion(){
		final Date fechaAdq=DateUtil.toDate("25/07/2003");
		final Date fechaAct=DateUtil.toDate("31/12/2006");
		final ActivoFijo af=new ActivoFijo();
		af.setFechaDeAdquisicion(fechaAdq);
		af.setFechaActualizacion(fechaAct);
		af.setMoi(BigDecimal.valueOf(841280.00));
		af.setInpc(104.3390);		
		af.setUltimoINPC(new INPC(2007,6,117.0590));
		
		assertEquals(BigDecimal.valueOf(1.1219), af.getFactorDeActualizacion());
	}
	
	public void testDepreciacionAcumulada(){
		final Date fechaAdq=DateUtil.toDate("25/07/2003");
		final Date fechaAct=DateUtil.toDate("31/12/2006");
		final ActivoFijo af=new ActivoFijo();
		af.setFechaDeAdquisicion(fechaAdq);
		af.setFechaActualizacion(fechaAct);
		af.setMoi(BigDecimal.valueOf(841280.00));
		af.setTasaDepreciacion(10.00);
		BigDecimal expected=BigDecimal.valueOf(287437.47);
		assertEquals(expected, af.getDepreciacionAcumulada());
	}

}
