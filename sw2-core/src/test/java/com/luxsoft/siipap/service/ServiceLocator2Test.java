package com.luxsoft.siipap.service;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Pruebas de acceso a los servicios prestados contenidos en {@link ServiceLocator2}
 * 
 * @author Ruben Cancino 
 *
 */
public class ServiceLocator2Test extends TestCase{
	
	private ServiceLocator2 sl;
	
	public void setUp(){
		sl=ServiceLocator2.instance();
	}
	
	public void testDbContext(){
		final ApplicationContext dbContext=sl.getContext();
		assertNotNull(dbContext);
		//DataSource
		DataSource ds=ServiceLocator2.getDataSource();
		assertNotNull(ds);
		JdbcTemplate template=ServiceLocator2.getJdbcTemplate();
		assertNotNull(template);
	}
		

	public void testServiceManagers(){
		for(Object bean:sl.getContext().getParent().getBeanDefinitionNames()){
			System.out.println("Bean: "+bean);
		}
		for(Object bean:sl.getContext().getBeanDefinitionNames()){
			System.out.println("Bean: "+bean);
		}
		for(ServiceManagers s:ServiceManagers.values()){
			assertNotNull("Debe existir el servicio: "+s.name(),sl.getContext().getBean(s.toString()));
		}
	}
	
	public void testLookupManager(){
		assertTrue(ServiceLocator2.getLookupManager().getBancos().size()>0);
	}

}
