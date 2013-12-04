package com.luxsoft.siipap.ventas.dao;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Asociado;
import com.luxsoft.siipap.ventas.model.Vendedor;

/**
 * Probamos el DAO de Cobrador
 * 
 * @author Ruben Cancino
 *
 */
public class AsociadoDaoTest extends BaseDaoTestCase{
	
	private Cliente cliente;
	private Vendedor vendedor;
	
	
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		cliente=(Cliente)universalDao.save(new Cliente("CC01","Cliente prueba"));
		vendedor=new Vendedor();
		vendedor.setApellidoP("CANCINO");
		vendedor.setApellidoM("RAMOS");
		vendedor.setNombres("RUBEN");
		vendedor.setCurp("CARR700317");
		vendedor.setRfc("CARR700317");		
		vendedor=(Vendedor)universalDao.save(vendedor);
	}




	public void testAddRemove(){
		
		Asociado a=new Asociado();
		a.setClave("AA001");
		a.setCliente(cliente);
		a.setVendedor(vendedor);
		a.setComisionCobrador(.5);
		a.setComisionVendedor(.5);
		a.setDireccion("Direccion");
		a.setNombre("ASOCIADO 1");
		a=(Asociado)universalDao.save(a);
		flush();
		assertNotNull(a.getId());
		
		universalDao.remove(Asociado.class, a.getId());
		flush();
		try {
			universalDao.get(Asociado.class, a.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}

}
