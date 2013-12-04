package com.luxsoft.siipap.dao.gastos;

import org.springframework.dao.DataRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GProductoServicio;

public class GProductoServicioDaoTest extends BaseDaoTestCase{	
	
	private GProductoServicioDao productoServicioDao;
	private GClasificacionDao clasificacionDao;
	
	public void testBuscar(){
		final String clave="TESTPROD1";
		GProductoServicio prod=productoServicioDao.buscarPorClave(clave);
		assertNotNull("Debe existir el registro  de SW_GPRODUCTOSERVICO clave:"+clave,prod);
	}
	
	@SuppressWarnings("unchecked")
	public void testAddRemove(){
		ConceptoDeGasto root=clasificacionDao.buscarPorClave("ROOT");
		assertNotNull("Debe existir el rubro root",root);
		GProductoServicio p=new GProductoServicio();		
		p.setClave("PRODUCTO1");
		p.setDescripcion("Producto de prueba 1");
		p.setRubro(root);
		p=productoServicioDao.save(p);
		assertNotNull(p.getId());
		flush();
		
		productoServicioDao.remove(p.getId());
		try {
			p=productoServicioDao.get(p.getId());
			fail("Se esperaba un error");
		} catch (DataRetrievalFailureException de) {
			assertNotNull(de);
		}
				
	}

	public void setProductoServicioDao(GProductoServicioDao productoServicioDao) {
		this.productoServicioDao = productoServicioDao;
	}

	public void setClasificacionDao(GClasificacionDao clasificacionDao) {
		this.clasificacionDao = clasificacionDao;
	}	
	

}
