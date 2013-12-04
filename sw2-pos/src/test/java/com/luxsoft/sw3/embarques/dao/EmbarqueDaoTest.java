package com.luxsoft.sw3.embarques.dao;



import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.EmbarqueForaneo;
import com.luxsoft.sw3.embarque.Incidente;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;


/**
 * Test basado en la nueva infra estructura de Spring TestContext
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EmbarqueDaoTest extends VentasBaseDaoTest2{
	
	
	@Autowired
	protected SucursalDao sucursalDao;
	
	@Autowired
	protected ProductoDao productoDao;
	
	@Autowired
	protected UniversalDao universalDao;
	
	
	private Sucursal sucursal;
	
	private Transporte transporte;
	
	@BeforeTransaction
	public void setUp(){		
		sucursal=sucursalDao.buscarPorClave(3);
		assertNotNull(sucursal);
		transporte=(Transporte)universalDao.getAll(Transporte.class).get(0);
		assertNotNull(transporte);
	}
	
	
	
	@Test	
	public void salvarElimiar(){
		Embarque embarque=new Embarque();		
		embarque.setFecha(new Date());
		embarque.setComentario("EMBARQUE DE PRUEBA");		
		embarque.setSucursal(sucursal.getNombre());
		embarque.setTransporte(transporte);
		embarque.setSalida(new Date());
		embarque.getIncidentes().add(new Incidente("INCIDENTE DE PRUEBA"));
		
		embarque=(Embarque)universalDao.save(embarque);
		flush();
		
		embarque=(Embarque)universalDao.get(Embarque.class,embarque.getId());
		
		assertEquals("EMBARQUE DE PRUEBA", embarque.getComentario());
		assertNotNull(embarque.getId());
		
		logger.info("Eliminando pedido: "+embarque.getId());
		universalDao.remove(Embarque.class,embarque.getId());
		flush();
		
		try {
			embarque=(Embarque)universalDao.get(Embarque.class,embarque.getId());
			fail("No debio encotrar el embarque: "+embarque.getId());
		} catch (ObjectRetrievalFailureException ore) {
			logger.debug("Expected exception: "+ore.getMessage());
			assertNotNull(ore);
		}
	}
	
	
	@Test	
	public void salvarForaneo(){
		EmbarqueForaneo embarque=new EmbarqueForaneo();		
		embarque.setFecha(new Date());
		embarque.setComentario("EMBARQUE FORANEO DE PRUEBA");		
		embarque.setSucursal(sucursal.getNombre());
		embarque.setTransporte(transporte);
		embarque.setSalida(new Date());
		embarque.getIncidentes().add(new Incidente("INCIDENTE DE PRUEBA"));
		
		
		embarque=(EmbarqueForaneo)universalDao.save(embarque);
		flush();
		
		embarque=(EmbarqueForaneo)universalDao.get(EmbarqueForaneo.class,embarque.getId());
		
		assertEquals("EMBARQUE FORANEO DE PRUEBA", embarque.getComentario());
		assertNotNull(embarque.getId());
		
		logger.info("Eliminando ambarque: "+embarque.getId());
		universalDao.remove(Embarque.class,embarque.getId());
		flush();
		
		try {
			embarque=(EmbarqueForaneo)universalDao.get(EmbarqueForaneo.class,embarque.getId());
			fail("No debio encotrar el embarque: "+embarque.getId());
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
		fail("TO BE IMPLEMENTED");
	}
	
	
}
