package com.luxsoft.siipap.dao.tesoreria;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.tesoreria.Concepto;

/**
 * Prueba la persistencia de los tipos de ingreso
 * 
 * @author Ruben Cancino
 *
 */
public class IngresoEgresoTipoDaoTest extends BaseDaoTestCase{
	
	
	public void testAddRemove(){
		
		Concepto tipo=new Concepto("Ventas","Ingreso por ventas de contado");
		tipo=(Concepto)universalDao.save(tipo);
		flush();
		
		assertEquals(tipo.getClave(),"Ventas" );
		assertNotNull(tipo.getId());
		
		log.debug("Eliminando Tipo");
		universalDao.remove(Concepto.class, tipo.getId());
		flush();
			
		try {
			tipo=(Concepto)universalDao.get(Concepto.class, tipo.getId());
			fail("Se esperaba una excepcion ");
		} catch (ObjectRetrievalFailureException e) {
			log.debug("Expected exception: "+e.getMessage());
			assertNotNull(e);
		}
	}

}
