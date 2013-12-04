package com.luxsoft.sw3.ventas.dao;


import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.Rollback;

import com.luxsoft.sw3.ventas.Factura;
import com.luxsoft.sw3.ventas.FacturaFolio;
import com.luxsoft.sw3.ventas.Pedido;

import static org.junit.Assert.*;

/**
 * Prueba de persistencia para entidades {@link FacturaDao}}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FacturaDaoTest extends VentasBaseDaoTest2{
	
	@Autowired
	PedidoDao pedidoDao;
	@Autowired
	FacturaDao facturaDao;
	
	Pedido pedido;
	
	@Before
	@NotTransactional
	public void setUp(){
		//pedido=pedidoDao.get(new Long(1));
		//assertNotNull(pedido);
	}

	@Test
	public void persist(){
		Factura fac=new Factura();
		fac.setPedido(pedido);
		fac.setFecha(new Date());
		fac.setFolioAutomatico(new FacturaFolio());
		fac.setComentario2("TEST FAC");
		fac=facturaDao.save(fac);
		flush();
		assertNotNull(fac.getId());
		
		fac=facturaDao.get(fac.getId());
		assertNotNull(fac.getFolioAutomatico());
		assertEquals("TEST FAC",fac.getComentario2());
	}
	
	@Test
	@Rollback(false)
	public void leftData(){
		Factura fac=new Factura();
		fac.setPedido(pedido);
		fac.setFecha(new Date());
		fac.setFolioAutomatico(new FacturaFolio());
		fac.setComentario2("TEST FAC");
		fac=facturaDao.save(fac);
		flush();
		assertNotNull(fac.getId());
		
		
	}
}
