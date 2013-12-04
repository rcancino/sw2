package com.luxsoft.sw3.dao;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.BeforeTransaction;

import static junit.framework.Assert.*;

import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

public class CajaDaoTest extends VentasBaseDaoTest2{
	
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
		Caja c=new Caja();
		c.setSucursal(sucursal);
		c.setCaja(BigDecimal.valueOf(50000));
		c.setTipo(Caja.Tipo.EFECTIVO);
		c.setConcepto(Caja.Concepto.FONDO_FIJO);
		c.setPagos(BigDecimal.ZERO);
		c.setCorte(new Date());
		c=(Caja)universalDao.save(c);
		flush();
		assertNotNull(c.getId());
		
		c=(Caja)universalDao.get(Caja.class, c.getId());
		assertEquals(BigDecimal.valueOf(50000).doubleValue(), c.getCaja().doubleValue(),.0001);
		
		//Delete
		universalDao.remove(Caja.class, c.getId());
		flush();
		try {
			c=(Caja)universalDao.get(Caja.class, c.getId());
			fail("No debio localizar el registro: "+c.getId());
		} catch (ObjectRetrievalFailureException  e) {
			assertNotNull(e);
		}
	}
	
	@Test
	@NotTransactional
	public void leftData(){
				
		sucursal=sucursalDao.buscarPorClave(3);
		
		Caja c=new Caja();
		c.setSucursal(sucursal);
		c.setCaja(BigDecimal.valueOf(50000));
		c.setTipo(Caja.Tipo.EFECTIVO);
		c.setConcepto(Caja.Concepto.FONDO_FIJO);
		c.setPagos(BigDecimal.ZERO);
		c.setCorte(new Date());
		c=(Caja)universalDao.save(c);
		
		
		Caja c2=new Caja();
		c2.setSucursal(sucursal);
		c2.setCaja(BigDecimal.valueOf(50000));
		c2.setTipo(Caja.Tipo.MORRALLA);
		c2.setConcepto(Caja.Concepto.FONDO_FIJO);
		c2.setPagos(BigDecimal.ZERO);
		c2.setCorte(new Date());
		c2=(Caja)universalDao.save(c2);
		
		flush();
	}

}
