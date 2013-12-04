package com.luxsoft.sw3.contabilidad.dao;




import java.math.BigDecimal;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;




public class PolizaDaoTest extends BaseDaoTestCase{
	
	private PolizaDao polizaDao;
	
	private CuentaContableDao cuentaContableDao;
	
	private CuentaContable cuenta;
	
	
	
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
	}
	
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		cuenta=cuentaContableDao.buscarPorClave("102-2000-001");
		assertNotNull(cuenta);
	}


	public void testAddRemove(){
		
		Poliza poliza=new Poliza();
		poliza.setTipo(Poliza.Tipo.INGRESO);
		String des="Poliza de prueba";
		poliza.setDescripcion(des);
		poliza.setFolio(new Long(-999));
		
		PolizaDet debe=poliza.agregarPartida();
		debe.setDebe(new BigDecimal(25000));
		debe.setCuenta(cuenta);
		debe.setDescripcion("Cargo test");
		
		PolizaDet haber=poliza.agregarPartida();
		haber.setHaber(new BigDecimal(25000));
		haber.setCuenta(cuenta);
		haber.setDescripcion("Abono test");
		
		
		poliza=polizaDao.save(poliza);
		flush();
		assertNotNull(poliza.getId());
		
		poliza=polizaDao.get(poliza.getId());
		assertEquals(des, poliza.getDescripcion());		
		
		polizaDao.remove(poliza.getId());
		flush();
		
		try {
			poliza=polizaDao.get(poliza.getId());
			fail("No debe existir la poliza: "+poliza.getId());
		} catch (ObjectRetrievalFailureException e) {
			logger.info("OK poliza eliminada: "+ExceptionUtils.getRootCauseMessage(e));
		}
		//setComplete();
	}
	
	public void testAddPolza(){
		
		Poliza poliza=new Poliza();
		poliza.setTipo(Poliza.Tipo.INGRESO);
		String des="Poliza de prueba";
		poliza.setDescripcion(des);
		poliza.setFolio(new Long(-999));
		
		PolizaDet debe=poliza.agregarPartida();
		debe.setDebe(new BigDecimal(25000));
		debe.setCuenta(cuenta);
		debe.setDescripcion("Cargo test");
		
		PolizaDet haber=poliza.agregarPartida();
		haber.setHaber(new BigDecimal(25000));
		haber.setCuenta(cuenta);
		haber.setDescripcion("Abono test");
		
		
		poliza=polizaDao.save(poliza);
		flush();
		assertNotNull(poliza.getId());
		
		poliza=polizaDao.get(poliza.getId());
		assertEquals(des, poliza.getDescripcion());		
		
		setComplete();
	}
	


	public void setCuentaContableDao(CuentaContableDao cuentaContableDao) {
		this.cuentaContableDao = cuentaContableDao;
	}


	public void setPolizaDao(PolizaDao polizaDao) {
		this.polizaDao = polizaDao;
	}

	

	

}
