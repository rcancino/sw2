package com.luxsoft.siipap.dao.core;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.core.Linea;

public class LineaDaoTest extends BaseDaoTestCase{
	
	private GenericDao<Linea, Long> lineaDao;

	public void setLineaDao(GenericDao<Linea, Long> dao) {
		this.lineaDao = dao;
	}
	
	public void testAddRemove(){
		Linea l=new Linea();
		l.setNombre("LINEA1");
		l.setDescripcion("LINEA DE PRUEBA");
		l=lineaDao.save(l);
		flush();
		assertNotNull(l.getId());
		
		//Retrive
		l=lineaDao.get(l.getId());
		assertNotNull(l);
		assertEquals("LINEA1",l.getNombre());
		
		//Remove
		lineaDao.remove(l.getId());
		flush();
		try {
			l=lineaDao.get(l.getId());
			fail("Linea 'LINEA1' found in database");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e.getMessage());
		}
		
	}
	
	public void testAddChildren(){
		Linea l=new Linea();
		l.setNombre("LINEA1");
		l.setDescripcion("LINEA DE PRUEBA");
		
		for(int i=1;i<=10;i++){
			Linea child=new Linea();
			child.setNombre("LNEA_"+i);
			child.setDescripcion("DESC_"+i);
			l.agregarChildren(child);			
		}
		
		l=lineaDao.save(l);
		flush();
		
		for(Linea ll:l.getChildren()){
			Linea child=lineaDao.get(ll.getId());
			assertNotNull(child);
			assertEquals(l.getId(), child.getPadre().getId());
		}
		
		setComplete();
	}

}
