package com.luxsoft.siipap.dao.gastos;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.gastos.ClasificacionDeActivo;


public class ClasificacionDeActivoDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		ClasificacionDeActivo c=new ClasificacionDeActivo();
		c.setNombre("EQUIPO DE COMPUTO");
		c.setDescripcion("Todo el equipo de computo");
		c.setTasa(25.00);
		c=(ClasificacionDeActivo)universalDao.save(c);
		assertNotNull(c.getId());
		flush();
		//setComplete();
		universalDao.remove(ClasificacionDeActivo.class, c.getId());
		flush();
		try {
			c=(ClasificacionDeActivo)universalDao.get(ClasificacionDeActivo.class, c.getId());
			fail("Debiio mandar error");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("OK no encontro el bean");
		}
	}

}
