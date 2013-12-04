package com.luxsoft.sw3.embarques.dao;



import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;


/**
 * Test basado en la nueva infra estructura de Spring TestContext
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class TransporteDaoTest extends VentasBaseDaoTest2{
	
	
	
	@Autowired
	protected UniversalDao universalDao;
	
	
	private Chofer chofer;
	
	@BeforeTransaction
	public void setUp(){
		chofer=(Chofer)universalDao.get(Chofer.class, new Long(3));
		assertNotNull(chofer);
	}
	
	
	
	@Test	
	public void salvarElimiar(){
		Transporte transporte=new Transporte();		
		transporte.setChofer(chofer);
		transporte.setDescripcion("CAMIONETA 1");
		transporte.setPlacas("525-15XX");
		transporte.setPoliza("ACTIVA ID 3233");
		
		transporte=(Transporte)universalDao.save(transporte);
		flush();
		
		transporte=(Transporte)universalDao.get(Transporte.class,transporte.getId());
		
		assertEquals("CAMIONETA 1", transporte.getDescripcion());
		assertNotNull(transporte.getId());
		
		logger.info("Eliminando pedido: "+transporte.getId());
		universalDao.remove(Transporte.class,transporte.getId());
		flush();
		
		try {
			transporte=(Transporte)universalDao.get(Transporte.class,transporte.getId());
			fail("No debio encotrar : "+transporte.getId());
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
	@NotTransactional
	public void leftData(){
		if(universalDao.getAll(Transporte.class).isEmpty()){
			List<Chofer> list=universalDao.getAll(Chofer.class);
			for (int i = 0; i < list.size(); i++) {
				Transporte transporte=new Transporte();
				Chofer c=list.get(i);
				transporte.setChofer(c);
				transporte.setDescripcion("CAMIONETA "+c.getNombre());
				transporte.setPlacas(String.valueOf(System.currentTimeMillis()));
				transporte.setPoliza("ACTIVA ID "+c.getNombre());
				transporte=(Transporte)universalDao.save(transporte);
			}
		}
		
	}
	
	
}
