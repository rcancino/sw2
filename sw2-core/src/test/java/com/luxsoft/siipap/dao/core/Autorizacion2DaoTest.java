package com.luxsoft.siipap.dao.core;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.Autorizacion2;

/**
 * Probamos la persistencia de las autorizaciones
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Autorizacion2DaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Autorizacion2 a=new Autorizacion2();
		a.setAutorizo("ADMIN");
		a.setComentario("PRUEBA DE AUTORIZACION");
		a=(Autorizacion2)universalDao.save(a);
		flush();
		assertNotNull(a.getId());
		
		a=(Autorizacion2)universalDao.get(Autorizacion2.class, a.getId());
		logger.info(a);
		setComplete();
		
	}

}
