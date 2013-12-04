package com.luxsoft.siipap.dao.tesoreria;


import org.springframework.dao.DataAccessException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.tesoreria.Banco;

public class BancoDaoTest extends BaseDaoTestCase{
	
	

	public void testAddRemoveBanco(){
		Empresa emp=(Empresa)universalDao.get(Empresa.class, new Long(1));
		assertNotNull(emp);
		
		Banco b=new Banco();
		b.setEmpresa(emp);
		b.setClave("BANAMEX");
		b.setNombre("Banco nacional mexicano");
		b.setRfc("RFC");
		
		b=(Banco)universalDao.save(b);
		flush();
		//setComplete();
		
		assertEquals("BANAMEX", b.getClave());
		assertNotNull(b.getId());
		
		
		log.debug("Eliminando banco");
		universalDao.remove(Banco.class, b.getId());
		flush();
		
		try {
			universalDao.get(Banco.class, b.getId());
			fail("Banco encontrado en la base de datos");
		} catch (DataAccessException e) {
			log.debug("Expected exception: "+e.getMessage());
			assertNotNull(e);
		}
		
	}
	

}
