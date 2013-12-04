package com.luxsoft.siipap.dao.gastos;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.gastos.Consignatario;

public class ConsignatarioDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Consignatario c=new Consignatario();
		c.setApellidoP("HERNANDEZ");
		c.setApellidoM("GARCIA");
		c.setNombres("RAUL");
		c=(Consignatario)universalDao.save(c);
		assertNotNull(c.getId());
		flush();
		
		universalDao.remove(Consignatario.class, c.getId());
		try {
			c=(Consignatario)universalDao.get(Consignatario.class, c.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("ERROR OK");
		}
	}

}
