package com.luxsoft.sw3.embarques.dao;



import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;



/**
 * Test basado en la nueva infra estructura de Spring TestContext
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ChoferDaoTest extends VentasBaseDaoTest2{
	
	
	
	
	@Autowired
	protected UniversalDao universalDao;
	
	
	
	
	@Test	
	public void salvarElimiar(){
		Chofer chofer=new Chofer();		
		chofer.setNombre("CHOFER DE PREUBA 1");
		chofer.setRadio("SIN RADIO");
		chofer.setRfc("");
		
		chofer=(Chofer)universalDao.save(chofer);
		flush();
		
		chofer=(Chofer)universalDao.get(Chofer.class,chofer.getId());
		
		assertEquals("CHOFER DE PREUBA 1", chofer.getNombre());
		assertNotNull(chofer.getId());
		
		logger.info("Eliminando chofer: "+chofer.getId());
		universalDao.remove(Chofer.class,chofer.getId());
		flush();
		
		try {
			chofer=(Chofer)universalDao.get(Chofer.class,chofer.getId());
			fail("No debio encotrar : "+chofer.getId());
		} catch (ObjectRetrievalFailureException ore) {
			logger.debug("Expected exception: "+ore.getMessage());
			assertNotNull(ore);
		}
	}

	/**
	 * Test and left some data in the DB
	 * 
	 */
	@Test
	@Rollback(false)
	public void leftData(){
		for(int i=1;i<=3;i++){
			Chofer chofer=new Chofer();		
			chofer.setNombre("CHOFER_"+i);
			chofer.setRadio("SIN RADIO");
			chofer.setRfc("");		
			chofer=(Chofer)universalDao.save(chofer);
			flush();
		}
		
	}
	
	
}
