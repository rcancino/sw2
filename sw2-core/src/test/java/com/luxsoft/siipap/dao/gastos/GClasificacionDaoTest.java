package com.luxsoft.siipap.dao.gastos;

import org.springframework.dao.DataRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;



public class GClasificacionDaoTest extends BaseDaoTestCase{
	
	private GClasificacionDao clasificacionDao;
	
	@SuppressWarnings("unchecked")
	public void testGetClasificacionPorClave(){
		final ConceptoDeGasto root=clasificacionDao.buscarPorClave("root");
		assertNotNull("Debe existir la clasificacion general",root);		
	}
	
	@SuppressWarnings("unchecked")
	public void testAddRemove(){
		ConceptoDeGasto limpieza=new ConceptoDeGasto("LIMPIEZA","CATEGORIA GENERAL");
		final ConceptoDeGasto root=clasificacionDao.buscarPorClave("root");
		limpieza.setParent(root);
		limpieza=clasificacionDao.save(limpieza);
		
		assertNotNull(limpieza.getId());
		flush();		
		
		clasificacionDao.remove(limpieza.getId());
		
		try {
			limpieza=clasificacionDao.get(limpieza.getId());
			fail("Se esperaba un error");
		} catch (DataRetrievalFailureException de) {
			assertNotNull(de);
		}
		
		
	}

	public void setClasificacionDao(GClasificacionDao clasificacionDao) {
		this.clasificacionDao = clasificacionDao;
	}

	
	
	

}
