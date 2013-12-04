package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import org.springframework.dao.DataRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;

public class GTipoProveedorDaoTest extends BaseDaoTestCase{
	
	private UniversalDao universalDao;
	@SuppressWarnings("unchecked")
	public void testGetTipos(){
		final List<GTipoProveedor> tipos=universalDao.getAll(GTipoProveedor.class);
		assertFalse("Debe existir por lo menos un GTipo",tipos.isEmpty());		
	}
	
	@SuppressWarnings("unchecked")
	public void testAddRemove(){
		GTipoProveedor tipo=new GTipoProveedor("OTROS","SIN CATEGORIA");
		final List<GTipoProveedor> tipos=universalDao.getAll(GTipoProveedor.class);
		tipo.setParent(tipos.get(0));
		tipo=(GTipoProveedor)universalDao.save(tipo);
		assertNotNull(tipo.getId());
		flush();
		universalDao.remove(GTipoProveedor.class, tipo.getId());
		try {
			tipo=(GTipoProveedor)universalDao.get(GTipoProveedor.class, tipo.getId());
			fail("Se esperaba un error");
		} catch (DataRetrievalFailureException de) {
			assertNotNull(de);
		}
		
		
	}

	public void setUniversalDao(UniversalDao universalDao) {
		this.universalDao = universalDao;
	}
	
	

}
