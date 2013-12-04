package com.luxsoft.siipap.ventas.dao;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.ventas.model.Vendedor;

/**
 * Probamos el DAO de Cobrador
 * 
 * @author Ruben Cancino
 *
 */
public class VendedorDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Vendedor c=new Vendedor();
		c.setApellidoP("CANCINO");
		c.setApellidoM("RAMOS");
		c.setNombres("RUBEN");
		c.setCurp("CARR700317");
		c.setRfc("CARR700317");
		
		c=(Vendedor)universalDao.save(c);
		flush();
		assertNotNull(c.getId());
		
		universalDao.remove(Vendedor.class, c.getId());
		flush();
		try {
			universalDao.get(Vendedor.class, c.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}

}
