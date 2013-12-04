package com.luxsoft.sw3.dao;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.Repeat;

import com.luxsoft.siipap.compras.dao.Compra2Dao;
import com.luxsoft.siipap.compras.dao.ProveedorDao;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

public class Compra2DaoTest extends VentasBaseDaoTest2{
	
	@Autowired
	Compra2Dao compra2Dao;	
	@Autowired
	ProveedorDao proveedorDao;	
	@Autowired
	ProductoDao productoDao;
	@Autowired
	protected SucursalDao sucursalDao;
	
	private Proveedor proveedor;
	private List<Producto> productos;
	private Sucursal sucursal;
	
	//@BeforeTransaction
	@Before
	public void setUp(){
		sucursal=sucursalDao.buscarPorClave(3);
		assertNotNull(sucursal);
		proveedor=proveedorDao.buscarPorClave("I001");
		assertNotNull(proveedor);
		productos=new ArrayList<Producto>();
		productos.add(productoDao.buscarPorClave("POL74"));
		productos.add(productoDao.buscarPorClave("PB36"));
		assertFalse(productos.isEmpty());
	}
	
	@Test
	public void testAddRemoveCompra(){
		Compra2 compra=new Compra2();
		compra.setComentario("COMPRA DE PRUEBA");
		compra.setFecha(new Date());
		compra.setProveedor(proveedor);
		compra.setSucursal(sucursal);
		
		for(Producto p:productos){
			CompraUnitaria det=new CompraUnitaria(p);
			det.setPrecio(BigDecimal.valueOf(p.getPrecioContado()));
			det.setDesc1(30);
			det.setSolicitado(10000);
			det.setSucursal(sucursal);
			compra.agregarPartida(det);
		}
		
		compra=compra2Dao.save(compra);
		flush();
		assertNotNull(compra.getId());
		compra=compra2Dao.get(compra.getId());
		assertEquals("COMPRA DE PRUEBA", compra.getComentario());
		for(CompraUnitaria det:compra.getPartidas()){
			assertNotNull(det.getId());
			assertEquals(10000, det.getSolicitado(),.0005);
		}
		
		//Remove
		compra2Dao.remove(compra.getId());
		flush();
		try {
			compra2Dao.get(compra.getId());
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
			logger.info("OK error esperado");
		}
	}
	
	@Test
	@NotTransactional @Repeat(3)
	public void leftData(){
		Compra2 compra=new Compra2();
		compra.setComentario("COMPRA DE PRUEBA");
		compra.setFecha(new Date());
		compra.setProveedor(proveedor);
		compra.setSucursal(sucursal);
		
		for(Producto p:productos){
			CompraUnitaria det=new CompraUnitaria(p);
			det.setPrecio(BigDecimal.valueOf(p.getPrecioContado()));
			det.setDesc1(30);
			det.setSolicitado(10000);
			det.setSucursal(sucursal);
			compra.agregarPartida(det);
		}
		
		compra=compra2Dao.save(compra);
		//flush();
	}

	/*@Test
	public void testInicializarCompra() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuscarCompras() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuscarComprasPendientesPorProveedor() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuscarPorFolio() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuscarPartidas() {
		fail("Not yet implemented");
	}*/

}
