package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.Empresa;

/**
 * Prueba el comportamiento y estado de la Transferencia
 * 
 * Verificamos basicamente 3 cosas
 * 	Que las regla para generar los cargo/abono se cumplan
 * 	Que los importes sean consistentes
 *  Que se genera un cargo por comision si esta es necesaria
 * 
 * @author Ruben Cancino
 *
 */
public class TransferenciaTest extends TestCase{
	
	private Logger logger=Logger.getLogger(getClass());
	
	public void testGenerarCargoAbonoSiExisteImporte(){
		
		Cuenta origen=new Cuenta();
		Cuenta destino=new Cuenta();
		Transferencia t=new Transferencia();
		t.setOrigen(origen);
		t.setDestino(destino);
		
		try {
			t.generarCargoAbono();
			fail("Debe mandar error por que el importe no ha sido fijado");
		} catch (TransferenciaException ex) {
			assertNotNull(ex);
			logger.debug("Error expected OK");
		}
	}
	
	public void testGenerarCargoAbonoUnaSolaVez(){
		Empresa e=new Empresa("Papelsa","Papel SA");
		Banco b=new Banco("BancoT","Banco Test");
		b.setEmpresa(e);
		BigDecimal importe=BigDecimal.valueOf(750000);
		Cuenta origen=new Cuenta(b);
		Cuenta destino=new Cuenta(b);
		Transferencia t=new Transferencia();
		t.setOrigen(origen);
		t.setDestino(destino);
		t.setImporteOri(importe);
		t.setTc(BigDecimal.ONE);
		t.generarCargoAbono(); //Una y solo una vez
		
		try {
			t.generarCargoAbono();
			fail("Debe mandar error por que los cargos ya fueron generados");
		} catch (TransferenciaException ex) {
			assertNotNull(ex);
			logger.debug("Error expected OK");
		}
	}

	public void testConsistenciaDeCargoAbono(){
		Empresa e=new Empresa("Papelsa","Papel SA");
		Banco b=new Banco("BancoT","Banco Test");
		b.setEmpresa(e);
		
		
		BigDecimal importe=BigDecimal.valueOf(750000);
		Cuenta origen=new Cuenta(b);
		Cuenta destino=new Cuenta(b);
		Transferencia t=new Transferencia();
		t.setOrigen(origen);
		t.setDestino(destino);
		t.setImporteOri(importe);
		t.setTc(BigDecimal.ONE);
		
		CargoAbono[] movs=t.generarCargoAbono();
		
		CargoAbono cargo=movs[0];
		//assertTrue(cargo.getConcepto().getTipo().equals(Tipo.CARGO));
		CargoAbono abono=movs[1];
		//assertTrue(abono.getConcepto().getTipo().equals(Tipo.ABONO));
		
		assertTrue(cargo==t.getCargo());
		assertEquals(importe, cargo.getImporte().multiply(BigDecimal.valueOf(-1)));		
		
		assertTrue(abono==t.getAbono());
		assertEquals(importe, abono.getImporte());
		assertEquals(importe, t.getImporteDest());
	}
	
	public void testComision(){
		Empresa e=new Empresa("Papelsa","Papel SA");
		Banco b=new Banco("BancoT","Banco Test");
		b.setEmpresa(e);
		
		
		BigDecimal importe=BigDecimal.valueOf(750000);
		Cuenta origen=new Cuenta(b);
		Cuenta destino=new Cuenta(b);
		Transferencia t=new Transferencia();
		t.setOrigen(origen);
		t.setDestino(destino);
		t.setImporteOri(importe);
		t.setTc(BigDecimal.ONE);
		t.setComision(BigDecimal.valueOf(750));
		t.generarCargoAbono();
		
		CargoAbono cargo=t.generarCargoPorComision();
		assertNotNull(cargo);
		assertEquals(BigDecimal.valueOf(-750), cargo.getImporte());
		try {
			t.generarCargoPorComision();
			fail("Debe mandar error en virtud de que el cargo por comision ya ha sido generado");
		} catch (TransferenciaException ex) {
			assertNotNull(ex);
			logger.debug("Error ok");
		}
		
	}
	
	
}
