package com.luxsoft.siipap.dao;

import java.util.List;

import org.springframework.dao.DataRetrievalFailureException;

import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;


public class SucursalDaoTest extends BaseDaoTestCase{
	
	private SucursalDao sucursalDao;
	
	
	public void testGetAll(){
		List<Sucursal> l=sucursalDao.getAll();
		assertFalse("Debe existir por lo menos una sucursal",l.isEmpty());
	}
	
	public void testAddRemove(){
		Empresa emp=(Empresa)universalDao.get(Empresa.class, new Long(1));
		assertNotNull(emp);
		Sucursal s=new Sucursal(emp,55,"TestAndrade");
		s=sucursalDao.save(s);
		assertNotNull(s.getId());
		flush();
		
		sucursalDao.remove(s.getId());
		try{
			s=sucursalDao.get(s.getId());
			fail("Se esperaba un DataRetribalFailureException");
		}catch(DataRetrievalFailureException e){
			assertNotNull(e);
		}
	}
	
	

	public void setSucursalDao(SucursalDao sucursalDao) {
		this.sucursalDao = sucursalDao;
	}

	
	

}
