package com.luxsoft.siipap.dao.gastos;

import java.math.BigDecimal;

import org.springframework.dao.DataRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.cxp.CxP2;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.GProveedor;

public class GCompraDaoTest extends BaseDaoTestCase{
	
	private GCompraDao compraDao;
	
	private GProveedorDao proveedorDao;
	private GenericDao<Departamento, Long> departamentoDao;
	private GenericDao<Sucursal,Long> sucursalDao;
	
	/*
	public void testGetByDateProveedor(){
		final GProveedor p=proveedorDao.buscarPorClave("PROV01");
		assertNotNull("Debe existir el proveedor PROV01",p);
		final List<GCompra> compras=compraDao.buscarPorProveedor(p);
		assertFalse("Debe existir por lo menos una compra",compras.isEmpty());
	}
	*/
	
	public void testAddRemove(){
		final GProveedor p=proveedorDao.get(99l);
		final Departamento depto=departamentoDao.getAll().get(0);
		final Sucursal sucursal=sucursalDao.getAll().get(0);
		
		GCompra compra=new GCompra();
		compra.setProveedor(p);
		compra.setDepartamento(depto);
		compra.setSucursal(sucursal);
		compra.calcularVencimiento();
		compra.autorizar("RCANCINO");
		compra.setComentario("Compra 1");
		compra.estimarFechaDeEntrega(10);
		
		/** Agregando partidas **/
		assertFalse("El proveedor debe tener por lo menos un producto ",p.getProductos().isEmpty());
		final GProductoServicio prod=p.getProductos().iterator().next().getProducto();
		GCompraDet partida=compra.generarPartida(prod);
		partida.setCantidad(BigDecimal.valueOf(5));
		partida.setPrecio(BigDecimal.valueOf(50));		
		assertTrue(partida.validarCantidad());
		assertTrue(partida.validarDescuentos());
		assertTrue(partida.validarPrecio());
		assertNotNull(partida);
		
		compra=compraDao.save(compra);
		assertNotNull(compra.getId());
		flush();
		//setComplete();
		
		compraDao.remove(compra.getId());
		flush();
		
		
		compra=compraDao.get(compra.getId());
		assertNull(compra);
	}
	
	/**
	 * TODO modificar este test para operar con una compra ya existente en la base de datos
	 * 
	 *
	 */
	public void testAddRemoveCxP(){
		final GProveedor p=proveedorDao.get(99l);
		final Departamento depto=departamentoDao.getAll().get(0);
		final Sucursal sucursal=sucursalDao.getAll().get(0);
		
		GCompra compra=new GCompra();
		compra.setProveedor(p);
		compra.setDepartamento(depto);
		compra.setSucursal(sucursal);
		compra.calcularVencimiento();
		compra.autorizar("RCANCINO");
		compra.setComentario("Compra 1");
		compra.estimarFechaDeEntrega(10);
		
		/** Agregando partidas **/
		assertFalse("El proveedor debe tener por lo menos un producto ",p.getProductos().isEmpty());
		final GProductoServicio prod=p.getProductos().iterator().next().getProducto();
		GCompraDet partida=compra.generarPartida(prod);
		assertNotNull(partida);
		
		partida.setCantidad(BigDecimal.valueOf(5));
		partida.setPrecio(BigDecimal.valueOf(50));		
		compra.actualizar();
			
		GFacturaPorCompra cxp=compra.crearCuentaPorPagar();
		cxp.setDocumento("A45685");
		compra.agregarFactura(cxp);
		compra=compraDao.save(compra);
		flush();
		
		assertNotNull(compra.getId());
		assertFalse(compra.getFacturas().isEmpty());
		assertNotNull(compra.getFacturas().iterator().next().getId());
		
		cxp=(GFacturaPorCompra)universalDao.get(GFacturaPorCompra.class, compra.getFacturas().iterator().next().getId());
		assertNotNull(cxp.getId());
		
		flush();
		setComplete();
		/*
		compraDao.remove(compra.getId());
		flush();
		
		
		compra=compraDao.get(compra.getId());
		assertNull(compra);
			*/	
		
	}
	
	
	public void setCompraDao(GCompraDao compraDao) {
		this.compraDao = compraDao;
	}

	public void setDepartamentoDao(GenericDao<Departamento, Long> departamentoDao) {
		this.departamentoDao = departamentoDao;
	}

	public void setProveedorDao(GProveedorDao proveedorDao) {
		this.proveedorDao = proveedorDao;
	}

	public void setSucursalDao(GenericDao<Sucursal, Long> sucursalDao) {
		this.sucursalDao = sucursalDao;
	}

	

}
