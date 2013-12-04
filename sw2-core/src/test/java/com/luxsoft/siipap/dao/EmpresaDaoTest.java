package com.luxsoft.siipap.dao;

import java.util.Locale;

import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.Empresa;

public class EmpresaDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Empresa e=new Empresa();
		e.setClave("PAPELSATEST");
		e.setNombre("PAPEL TEST S.A. de C.V.");
		Direccion d=new Direccion();
		d.setCalle("Biólogo Maximino Martínez");
		d.setCiudad("México D.C.");
		d.setColonia("San Salvador Xochimanca");
		d.setCp("02870");
		d.setEstado("Mexico D.F.");
		d.setLocalidad(new Locale("es","mx"));
		d.setMunicipio("");
		d.setNumero("3902");
		d.setNumeroInterior("3902");
		d.setPais("MEXICO");		
		e.setDireccion(d);
		e.setDescripcion("Empresa primaria del sistema");
		e.setRfc("PAP830101CR3");
		
		e=(Empresa)universalDao.save(e);
		flush();
		
		assertEquals("PAPELSATEST", e.getClave());
		assertNotNull(e.getId());
		setComplete();
		
		
	}

		

}
