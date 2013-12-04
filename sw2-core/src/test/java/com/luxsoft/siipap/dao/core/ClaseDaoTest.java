package com.luxsoft.siipap.dao.core;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.core.Clase;

public class ClaseDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Clase m=new Clase();
		m.setNombre("GENERICA2");
		m=(Clase)universalDao.save(m);
		flush();
		assertNotNull(m.getId());		
	}

}
