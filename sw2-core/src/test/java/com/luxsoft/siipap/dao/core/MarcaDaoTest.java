package com.luxsoft.siipap.dao.core;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.core.Marca;

public class MarcaDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		Marca m=new Marca();
		m.setNombre("GENERICA2");
		m=(Marca)universalDao.save(m);
		flush();
		assertNotNull(m.getId());
		
	}

}
