package com.luxsoft.sw3.dao;

import static junit.framework.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.caja.Gasto;
import com.luxsoft.sw3.caja.ProductoServicio;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

import static junit.framework.Assert.*;

public class GastoDaoTest extends VentasBaseDaoTest2{
	
	@Autowired
	private UniversalDao universalDao;
	
	@Autowired
	protected SucursalDao sucursalDao;
	
	private Sucursal sucursal;
	
	@BeforeTransaction
	public void setUp(){
		sucursal=sucursalDao.buscarPorClave(3);
		assertNotNull(sucursal);
	}
	
	@Test
	public void addRemove(){
		BigDecimal importe=BigDecimal.valueOf(500.00);
		Gasto gasto=new Gasto();
		gasto.setSucursal(sucursal);
		gasto.setImporte(importe);
		gasto.setComentario("Prueba de Gasto en el punto de venta");
		gasto.setProductoServicio(new ProductoServicio(1l,"AGUA NATURAL"));
		gasto=(Gasto)universalDao.save(gasto);
		flush();
		gasto=(Gasto)universalDao.get(Gasto.class, gasto.getId());
		assertNotNull(gasto.getId());
		
		assertEquals(importe.doubleValue(), gasto.getImporte().doubleValue(),.0001d);
		assertEquals("AGUA NATURAL", gasto.getDescripcion());
		
		//Delete
		universalDao.remove(Gasto.class,gasto.getId());
		flush();
		
		try {
			gasto=(Gasto)universalDao.get(Gasto.class, gasto.getId());
			fail("No debio encotrar el gasto:"+gasto.getId());
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
		
	}
	
	

}
