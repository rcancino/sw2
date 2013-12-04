package com.luxsoft.siipap.dao.tesoreria;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Cuenta;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class CuentaDaoTest extends BaseDaoTestCase{
	
	
	public void testAddRemove(){
		 final Banco b=(Banco)universalDao.get(Banco.class, new Long(3));
		 assertTrue("Debe existir un banco",b!=null);
		 
		 Cuenta cta1=new Cuenta();
		 cta1.setBanco(b);
		 cta1.setClave("BANORTE2");
		 cta1.setDescripcion("Cuenta de prueba 1");
		 cta1.setCuentaContable("000-0000-001");
		 cta1.setNumero(4528l);
		 cta1=(Cuenta)universalDao.save(cta1);
		 
		 assertEquals("BANORTE2", cta1.getClave());
		 assertNotNull(cta1.getId());
		 setComplete();
		 
		 log.debug("Eliminando cuenta");
		 universalDao.remove(Cuenta.class, cta1.getId());
		 flush();
		 
		 try {
			 cta1=(Cuenta)universalDao.get(Cuenta.class, cta1.getId());
			 fail("Cuenta de Banco encontrada en la base de datos");
		} catch (ObjectRetrievalFailureException e) {
			log.debug("Expected exception: "+e.getMessage());
			assertNotNull(e);
		}
		 
	}

}
