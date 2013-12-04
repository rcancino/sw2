package com.luxsoft.sw3.services;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ServicesTxTest {
	
	private Services services;
	
	@Before
	public void setUp(){
		services=Services.getInstance();
	}
	
	/**
	 * Asegurarnos que la implementacion de PedidoManar es un proxy
	 * creado por Spring
	 */
	@Test
	public void pedidosManagerProxy(){
		PedidosManager manager=services.getPedidosManager();
		Class clazz=manager.getClass().getSuperclass();
		assertTrue(clazz==java.lang.reflect.Proxy.class);
		
		
	}

}
