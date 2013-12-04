package com.luxsoft.siipap.dao;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.model.Modulo;

/**
 * Prueba la persistencia de un {@link Modulo}
 * 
 * @author Ruben Cancino
 *
 */
public class ModuloDaoTest extends BaseDaoTestCase{
	
	@Transactional
	public void testAddRemove(){
		Modulo m=new Modulo("ModuloTest","Modulo de prueba");
		
		m=(Modulo)universalDao.save(m);
		assertNotNull(m.getId());
		flush();
		
		universalDao.remove(Modulo.class, m.getId());
		flush();
		
		
		try {
			m=(Modulo)universalDao.get(Modulo.class, m.getId());
			fail("Debio al no localizar el bean");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("OK No encontro el bean");
		}
		
	}

}
