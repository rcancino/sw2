package com.luxsoft.sw3.dao;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.core.AutorizacionClientePCE;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

public class AutorizacionClientePCETest extends VentasBaseDaoTest2{
	
	
	private Cliente cliente;
	
	@Autowired
	private UniversalDao universalDao;
	
	@Autowired
	private ClienteDao clienteDao;
	
	@BeforeTransaction
	public void setUp() throws Exception {
		cliente=clienteDao.buscarPorClave("U050008");
		assertNotNull(cliente);
	}

	@Test
	public void testAddRemove(){
		AutorizacionClientePCE aut=new AutorizacionClientePCE(cliente);
		aut=(AutorizacionClientePCE)universalDao.save(aut);
		flush();
		assertNotNull(aut.getId());
		aut=(AutorizacionClientePCE)universalDao.get(AutorizacionClientePCE.class, aut.getId());
		assertEquals(AutorizacionClientePCE.DESCRIPCION, aut.getComentario());
		
		//Delete
		universalDao.remove(AutorizacionClientePCE.class, aut.getId());
		flush();
		try {
			aut=(AutorizacionClientePCE)universalDao.get(AutorizacionClientePCE.class, aut.getId());
			fail("No debio encontrar la autorizacion: "+aut);
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}

	@Test
	@NotTransactional
	public void testLeftData(){
		cliente=clienteDao.buscarPorClave("S010501");
		assertNotNull(cliente);
		AutorizacionClientePCE aut=new AutorizacionClientePCE(cliente);
		aut=(AutorizacionClientePCE)universalDao.save(aut);
		
		
	}
}
