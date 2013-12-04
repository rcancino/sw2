package com.luxsoft.siipap.ventas.dao;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.ventas.model.Cobrador;

/**
 * Probamos el DAO de Cobrador
 * 
 * @author Ruben Cancino
 *
 */
public class CobradorDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Cobrador c=new Cobrador();
		c.setApellidoP("CANCINO");
		c.setApellidoM("RAMOS");
		c.setNombres("RUBEN");
		c.setCurp("CARR700317");
		c.setRfc("CARR700317");
		
		c=(Cobrador)universalDao.save(c);
		flush();
		assertNotNull(c.getId());
		
		universalDao.remove(Cobrador.class, c.getId());
		flush();
		try {
			universalDao.get(Cobrador.class, c.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}
	
	

}
