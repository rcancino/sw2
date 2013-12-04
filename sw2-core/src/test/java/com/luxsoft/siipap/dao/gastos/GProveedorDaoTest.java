package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import org.springframework.dao.DataRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;

public class GProveedorDaoTest extends BaseDaoTestCase{
	
	private UniversalDao universalDao;
	private GProveedorDao proveedorDeGastosDao;
	
	
	@SuppressWarnings("unchecked")
	public void testGetProveedores(){
		final GProveedor p=proveedorDeGastosDao.buscarPorNombre("Proveedor de prueba 1");
		assertNotNull("Debe existir el proveedor: Proveedor de prueba",p);		
	}

	@SuppressWarnings("unchecked")	
	public void testAddRemove(){
		GProveedor p=new GProveedor();		
		p.setNombre("Proveedor de prueba 2");		
		
		final List<GTipoProveedor> tipos=universalDao.getAll(GTipoProveedor.class);
		p.setTipo(tipos.get(0));
		
		p.agregarComentario("Proveedor princial");
		p.agregarComentario("Comentario adicional");
		p=proveedorDeGastosDao.save(p);
		assertNotNull(p.getId());
		flush();
		//setComplete();
		
		proveedorDeGastosDao.remove(p.getId());
		try {
			p=proveedorDeGastosDao.get(p.getId());
			fail("Se esperaba un error");
		} catch (DataRetrievalFailureException de) {
			assertNotNull(de);
		}		
	}
	
	
	@SuppressWarnings("unchecked")
	public void testAddProductos(){
		List<GProveedor> provs=proveedorDeGastosDao.getAll();
		assertFalse("Deven existir proveedores de prueba",provs.isEmpty());
		GProveedor p=provs.get(0);
		
		assertEquals(1, p.getProductos().size());
		List<GProductoServicio> prods=universalDao.getAll(GProductoServicio.class);
		for(GProductoServicio prod:prods){
			p.agregarProducto(prod);
		}
		
		p=proveedorDeGastosDao.save(p);
		flush();
		
		p=proveedorDeGastosDao.get(p.getId());
		assertEquals(prods.size(), p.getProductos().size());
		
		for(GProductoServicio prod:prods){
			p.removeProducto(prod);
		}
		p=proveedorDeGastosDao.save(p);
		flush();		
		assertEquals(0, p.getProductos().size());	
		
	}


	public void setProveedorDeGastosDao(GProveedorDao proveedorDao) {
		this.proveedorDeGastosDao = proveedorDao;
	}
	public void setUniversalDao(UniversalDao universalDao) {
		this.universalDao = universalDao;
	}
	
	
	

}
