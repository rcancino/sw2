package com.luxsoft.sw3.contabilidad.dao;




import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Tipo;

public class CuentaContableDaoTest extends BaseDaoTestCase{
	
	private CuentaContableDao cuentaContableDao;
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		
	}
	
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		
	}


	public void testAddRemove(){
		
		CuentaContable cuenta=new CuentaContable();
		final String clave="102-2000-000";
		cuenta.setClave(clave);
		cuenta.setDescripcion("Cuenta de mayor de PRUEBA");
		cuenta.setDescripcion2("DESC PRUEBA");
		cuenta.setTipo(Tipo.ACTIVO);
		
		cuenta=cuentaContableDao.save(cuenta);
		flush();
		assertNotNull(cuenta.getId());
		
		cuenta=cuentaContableDao.get(cuenta.getId());
		assertEquals(clave, cuenta.getClave());		
		
		cuentaContableDao.remove(cuenta.getId());
		flush();
		
		try {
			cuenta=cuentaContableDao.get(cuenta.getId());
			fail("No debe existir la cuenta: "+cuenta.getId());
		} catch (ObjectRetrievalFailureException e) {
			logger.info("OK cuenta eliminada: "+ExceptionUtils.getRootCauseMessage(e));
		}
		//setComplete();
	}
	
	public void testAddCuentaConSubCuentas(){
		CuentaContable cuenta=new CuentaContable();
		final String clave="102-2000-000";
		cuenta.setClave(clave);
		cuenta.setDescripcion("Cuenta de mayor de PRUEBA");
		cuenta.setDescripcion2("DESC PRUEBA");
		cuenta.setTipo(Tipo.ACTIVO);
		
		CuentaContable subCuenta=new CuentaContable();
		String subClave="102-2000-001";
		subCuenta.setClave(subClave);
		subCuenta.setDescripcion("Sub cuenta 1");
		subCuenta.setDetalle(true);
		
		cuenta.agregarCuenta(subCuenta);		
		cuenta=cuentaContableDao.save(cuenta);
		flush();
		assertNotNull(cuenta.getId());
		Long subId=cuenta.getSubCuentas().iterator().next().getId();
		subCuenta=cuentaContableDao.get(subId);
		assertEquals(subClave, subCuenta.getClave());
		
		setComplete();
	}


	public void setCuentaContableDao(CuentaContableDao cuentaContableDao) {
		this.cuentaContableDao = cuentaContableDao;
	}

	

	

}
