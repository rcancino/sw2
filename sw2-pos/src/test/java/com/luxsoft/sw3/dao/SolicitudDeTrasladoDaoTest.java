package com.luxsoft.sw3.dao;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;

import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

public class SolicitudDeTrasladoDaoTest extends VentasBaseDaoTest2{
	
	@Autowired
	ProductoDao productoDao;
	@Autowired
	protected SucursalDao sucursalDao;
	@Autowired
	protected UniversalDao universalDao;
	
	private Producto producto1;
	private Producto producto2;
	private Producto producto3;
	
	private Sucursal origen;
	private Sucursal destino;
	
	
	@Before
	public void setUp(){
		
		producto1=productoDao.buscarPorClave("POL74");
		producto2=productoDao.buscarPorClave("POL74.5");
		producto3=productoDao.buscarPorClave("POL74.5");
		
		origen=sucursalDao.get(2L);
		destino=sucursalDao.get(6L);
		
		assertNotNull(producto1);
		assertNotNull(producto2);
		assertNotNull(origen);
		assertNotNull(destino);
	}

	@NotTransactional
	@Test
	public void AddRemove(){
		Long docto=1L;
		SolicitudDeTraslado sol=new SolicitudDeTraslado();
		sol.setSucursal(destino);
		sol.setOrigen(origen);
		sol.setFecha(new Date());
		sol.setDocumento(docto);
		sol.setComentario("SOL DE PRUEBA");
		
		sol.agregarPartida(producto1, 5000d);
		sol.agregarPartida(producto2, 5000d);
		sol.agregarPartida(producto3, 5000d);
		
		
		sol=(SolicitudDeTraslado)universalDao.save(sol);
		assertNotNull(sol.getId());
		flush();
		
		sol=(SolicitudDeTraslado)universalDao.get(SolicitudDeTraslado.class, sol.getId());
		assertNotNull(sol);
		assertEquals(docto.longValue(),sol.getDocumento().longValue());
		
		//assertEquals(3,sol.getPartidas().size());
		
	}	

}
