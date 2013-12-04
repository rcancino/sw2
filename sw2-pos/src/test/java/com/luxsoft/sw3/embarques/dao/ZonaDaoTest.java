package com.luxsoft.sw3.embarques.dao;



import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.NotTransactional;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.sw3.embarque.Zona;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;



/**
 * Test basado en la nueva infra estructura de Spring TestContext
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ZonaDaoTest extends VentasBaseDaoTest2{
	
	
	
	
	@Autowired
	protected UniversalDao universalDao;
	
	
	
	
	@Test	
	public void salvarElimiar(){
		Zona zona=new Zona();		
		zona.setDescripcion("ZONA TEST");
		zona=(Zona)universalDao.save(zona);
		flush();
		
		zona=(Zona)universalDao.get(Zona.class,zona.getId());
		
		assertEquals("ZONA TEST", zona.getDescripcion());
		assertNotNull(zona.getId());
		
		logger.info("Eliminando : "+zona.getId());
		universalDao.remove(Zona.class,zona.getId());
		flush();
		
		try {
			zona=(Zona)universalDao.get(Zona.class,zona.getId());
			fail("No debio encotrar : "+zona.getId());
		} catch (ObjectRetrievalFailureException ore) {
			logger.debug("Expected exception: "+ore.getMessage());
			assertNotNull(ore);
		}
	}

	/**
	 * Test para dejar algunas instacias utilse en otras pruebas
	 * 
	 * TODO Pasar a un archvio 
	 */
	@Test
	@NotTransactional
	public void leftData(){
		if(universalDao.getAll(Zona.class).size()==0){
			for(int i=1;i<=3;i++){
				Zona zona=new Zona();		
				zona.setDescripcion("ZONA "+i);
				universalDao.save(zona);
				flush();
			}
		}
		
	}
	
	
}
