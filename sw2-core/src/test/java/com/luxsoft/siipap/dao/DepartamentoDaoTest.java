package com.luxsoft.siipap.dao;

import java.util.List;

import org.springframework.dao.DataRetrievalFailureException;

import com.luxsoft.siipap.model.Departamento;

public class DepartamentoDaoTest extends BaseDaoTestCase{
	
	private GenericDao<Departamento, Long> departamentoDao;
	
	@SuppressWarnings("unchecked")
	public void testBuscarTodos(){
		final List<Departamento> l=departamentoDao.getAll();
		assertTrue("Debe existir por lo menos un Departamento",!l.isEmpty());
	}
	
	public void testAddRemove(){
		Departamento d=new Departamento("Contabilidad","Contabilidad");
		d=departamentoDao.save(d);
		assertNotNull(d.getId());
		flush();
		departamentoDao.remove(d.getId());
		try{
			d=departamentoDao.get( d.getId());
			fail("Se esperaba un error");
		} catch (DataRetrievalFailureException de) {
			assertNotNull(de);
		}		
	}

	public void setDepartamentoDao(GenericDao<Departamento, Long> departamentoDao) {
		this.departamentoDao = departamentoDao;
	}

		

}
