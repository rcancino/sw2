package com.luxsoft.siipap.cxc.dao;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.cxc.model.Esquema;
import com.luxsoft.siipap.dao.BaseDaoTestCase;

/**
 * Prueba la persistencia de Esquemas de pago con tarjeta
 * de credito
 * 
 * @author Ruben Cancino
 *
 */
public class EsquemasDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Esquema e=new Esquema("3 MESES SIN INTERESES");
		e=(Esquema)universalDao.save(e);
		flush();
		
		e=(Esquema)universalDao.get(Esquema.class,e.getId());
		flush();
		assertNotNull(e.getId());
		
		universalDao.remove(Esquema.class,e.getId());
		flush();
		try {
			universalDao.get(Esquema.class, e.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e2) {
			assertNotNull(e2);
			logger.info(e2);
		}
		
	}

}
