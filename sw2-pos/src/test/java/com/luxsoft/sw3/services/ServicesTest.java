package com.luxsoft.sw3.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.service.core.ProveedorManager;

public class ServicesTest {
	
	private Services services;
	
	@Before
	public void setUp(){
		services=Services.getInstance();
	}

	@Test
	public void testGetDataSource() {
		DataSource ds=(DataSource)services.getDataSource();
		JdbcTemplate template=new JdbcTemplate(ds);
		Date hoy=(Date)template.queryForObject("select now()", Date.class);
		assertNotNull(hoy);
		System.out.println("Hoy: "+hoy);
	}

	@Test
	public void testGetJdbcTemplate() {
		JdbcTemplate template=services.getJdbcTemplate();
		Date hoy=(Date)template.queryForObject("select now()", Date.class);
		assertNotNull(hoy);
		System.out.println("Hoy: "+hoy);
	}

	@Test
	public void testGetInstance() {
		Services singleton=Services.getInstance();
		assertTrue(singleton==services);
	}
	
	
	
	@Test
	public void serviceLayer(){
		PedidosManager manager=services.getPedidosManager();
		assertNotNull(manager);
	}
	
	@Test
	public void clientesManager(){
		ClienteManager manager=services.getClientesManager();
		assertNotNull(manager);
	}
	
	@Test
	public void produtoManager(){
		ProductosManager2 manager=services.getProductosManager();
		assertNotNull(manager);
	}
	
	@Test
	public void proveedoresManager(){
		ProveedorManager manager=services.getProveedorManager();
		assertNotNull(manager);
	}
	
	@Test
	public void configuracion(){
		Configuracion manager=services.getConfiguracion();
		assertNotNull(manager);
	}
	
	@Test
	public void comprasManager(){
		ComprasManager manager=services.getComprasManager();
		assertNotNull(manager);
	}
	
	@Test
	public void inventariosManager(){
		InventariosManager manager=services.getInventariosManager();
		assertNotNull(manager);
	}
	
	@Test
	public void facturasManager(){
		FacturasManager manager=services.getFacturasManager();
		assertNotNull(manager);
	}

}
