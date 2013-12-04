package com.luxsoft.siipap.compras.dao;

import org.springframework.dao.DataAccessException;

import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;

public class ListaDePreciosDaoTest extends BaseDaoTestCase{
	
	private ListaDePreciosDao listaDePreciosDao;
	
	public void testAddRemove(){
		Proveedor proveedor=(Proveedor)universalDao.get(Proveedor.class, -99L);
		
	
		ListaDePrecios l=ListaDePrecios.createLista();
		l.setDescripcion("Lista de precios test");
		l.setProveedor(proveedor);
		l.setVigente(true);
		
		Producto prod1=(Producto)universalDao.get(Producto.class, 100L);		
		ListaDePreciosDet det=new ListaDePreciosDet();
		det.setProducto(prod1);
		det.setPrecio(CantidadMonetaria.pesos(500));
		det.setDescuento1(.1);
		det.setDescuento2(.03);
		l.agregarPrecio(det);
		
		Producto prod2=(Producto)universalDao.get(Producto.class, 500L);
		ListaDePreciosDet det2=new ListaDePreciosDet();
		det2.setProducto(prod2);
		det2.setPrecio(CantidadMonetaria.pesos(500));
		l.agregarPrecio(det2);
		
		l=listaDePreciosDao.save(l);
		flush();
		
		assertNotNull(l.getId());
		
		assertNotNull(l.getPrecios().iterator().next().getId());
		ListaDePreciosDet target=l.buscarPrecio(prod1);
		assertNotNull(target.getDescuento1());
		logger.info("Precio con descuentos: "+target+" Descs:"+target.getDescuento1());
	
		
		
		l=listaDePreciosDao.get(l.getId());
		assertEquals(2, l.getPrecios().size());		
		logger.info("Lista con precios: "+l.getPrecios());
		
		listaDePreciosDao.remove(l.getId());
		flush();
		try {
			listaDePreciosDao.get(l.getId());
			fail("No debio encontrar la lsita: "+l.getId());
		} catch (DataAccessException e) {
			assertNotNull(e);
		}
		
	}

	public void setListaDePreciosDao(ListaDePreciosDao listaDePreciosDao) {
		this.listaDePreciosDao = listaDePreciosDao;
	}
	
	

}
