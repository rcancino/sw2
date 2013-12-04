package com.luxsoft.siipap.ventas.dao;

import java.math.BigDecimal;
import java.util.List;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVentaDet;
import com.luxsoft.siipap.ventas.model.TipoDeLista;

public class ListaDePreciosVentaDaoTest extends BaseDaoTestCase{
	
	private TipoDeLista tipo;
	private ListaDePreciosVentaDao listaDePreciosVentaDao;
	private ProductoDao productoDao;
			
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		tipo=(TipoDeLista)universalDao.get(TipoDeLista.class, 1L);
		/*tipo=new TipoDeLista();
		tipo.setNombre("GENERICA");
		tipo.setDescripcion("LISTA_TEST_TIPO");
		tipo=(TipoDeLista)universalDao.save(tipo);
		*/
	}

	

	public void testAddRemove(){
		ListaDePreciosVenta lista=new ListaDePreciosVenta();
		lista.setComentario("Lista de prueba");
		
		
		List<Producto> prods=productoDao.buscarActivos();
		assertFalse("Deben existir productos de prueba",prods.isEmpty());
		
		for(Producto p:prods){
			if(p.isDeLinea()){
				ListaDePreciosVentaDet det=new ListaDePreciosVentaDet();
				det.setProducto(p);
				det.setComentario("Precio test");
				det.setPrecio(BigDecimal.valueOf(100));
				det.setCosto(BigDecimal.valueOf(80));
				det.setFactor(80/100);
				det.setProveedorClave("TTTT");
				det.setProveedorNombre("Proveedor Nombre");
				det.setPresentacion("Blanco");
				lista.agregarPrecio(det);
			}
		}
		
		lista=listaDePreciosVentaDao.save(lista);
		flush();
		assertNotNull(lista.getId());
		for(ListaDePreciosVentaDet det:lista.getPrecios()){
			assertNotNull(det.getId());
		}
		//setComplete();
		/*
		listaDePreciosVentaDao.remove(lista.getId());
		flush();
		try{
			lista=listaDePreciosVentaDao.get(lista.getId());
			fail("No debio encontrar la lista: "+lista.getId());
		}catch (ObjectRetrievalFailureException oe) {
			assertNotNull(oe);
		}
		*/
	}

	public void testAddRemove2(){
		ListaDePreciosVenta lista=new ListaDePreciosVenta();
		lista.setComentario("Lista de prueba especial 2");
		
		
		Long prodId=4002L;
		Producto p=(Producto)universalDao.get(Producto.class, prodId);
		assertNotNull(p);
		ListaDePreciosVentaDet det=new ListaDePreciosVentaDet();
		det.setProducto(p);
		det.setComentario("Precio test");
		det.setPrecio(BigDecimal.valueOf(100));
		det.setCosto(BigDecimal.valueOf(80));
		det.setFactor(80/100);
		det.setProveedorClave("TTTT");
		det.setProveedorNombre("Proveedor Nombre");
		det.setPresentacion("Blanco");
		lista.agregarPrecio(det);
		
		lista=listaDePreciosVentaDao.save(lista);
		flush();
		assertNotNull(lista.getId());
		for(ListaDePreciosVentaDet detalle:lista.getPrecios()){
			assertNotNull(detalle.getId());
			assertEquals(prodId, detalle.getProducto().getId());
		}
		setComplete();
	}
	
	public void setListaDePreciosVentaDao(
			ListaDePreciosVentaDao listaDePreciosVentaDao) {
		this.listaDePreciosVentaDao = listaDePreciosVentaDao;
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}
	
	

}
