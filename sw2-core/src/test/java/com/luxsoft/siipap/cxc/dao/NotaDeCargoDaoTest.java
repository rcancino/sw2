package com.luxsoft.siipap.cxc.dao;

import java.math.BigDecimal;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.ventas.model.Venta;

public class NotaDeCargoDaoTest extends BaseDaoTestCase{
	
	private CargoDao cargoDao;
	private HibernateTemplate hibernateTemplate;

	public void setCargoDao(CargoDao cargoDao) {
		this.cargoDao = cargoDao;
	}
	
	
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}


	public void testAddRemove(){
		
		Venta venta=(Venta)hibernateTemplate.iterate("from Venta v where v.saldo>0").next();
		
		NotaDeCargo cargo=CXCUtils.generaNotaDeCargo(venta);
		NotaDeCargoDet det=new NotaDeCargoDet();
		det.setVenta(venta);
		det.setComentario("CARGO TEST");
		det.setImporte(venta.getImporte());
		cargo.agregarConcepto(det);
		
		cargo=(NotaDeCargo)cargoDao.save(cargo);
		flush();
		
		assertNotNull(cargo.getId());
		assertEquals(1,cargo.getConceptos().size());
		//setComplete();
		cargoDao.remove(cargo.getId());
		flush();
		try {
			cargoDao.get(cargo.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
			
	}
	

}
