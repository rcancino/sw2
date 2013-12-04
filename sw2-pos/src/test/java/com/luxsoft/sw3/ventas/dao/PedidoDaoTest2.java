package com.luxsoft.sw3.ventas.dao;



import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;
import com.luxsoft.sw3.ventas.PedidoPendiente;


/**
 * Test basado en la nueva infra estructura de Spring TestContext
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoDaoTest2 extends VentasBaseDaoTest2{
	
	@Autowired
	protected PedidoDao pedidoDao;
	@Autowired
	protected ClienteDao clienteDao;
	@Autowired
	protected SucursalDao sucursalDao;
	@Autowired
	protected ProductoDao productoDao;
	@Autowired
	protected UniversalDao universalDao;
	
	private Cliente testCliente;
	private Sucursal sucursal;
	
	@BeforeTransaction
	public void setUp(){
		testCliente=clienteDao.buscarPorClave("U050008");
		assertNotNull(testCliente);
		sucursal=sucursalDao.buscarPorClave(3);
		assertNotNull(sucursal);
	}
	
	@Test
	public void getById(){
		//Pedido pedido=pedidoDao.get(new Long(2));
		//assertNotNull("Debe existir pedido de prueba en la base de datos",pedido);
	}	
	
	@Test
	//@Rollback(false)
	public void salvarElimiar(){
		
		Pedido p=new Pedido();
		p.setCliente(testCliente);
		p.setFecha(new Date());
		p.setComentario("Pedido de Prueba");
		p.setFormaDePago(FormaDePago.EFECTIVO);
		
		p.setSucursal(sucursal);
		
		PedidoDet det=PedidoDet.getPedidoDet();
		Producto prod=productoDao.buscarPorClave("POL74");
		assertNotNull("Debe existir el producto: POL74",prod);
		det.setProducto(prod);
		BigDecimal precio=BigDecimal.valueOf(prod.getPrecioContado());
		det.setPrecio(precio);
		det.setPrecioLista(precio);
		det.setCantidad(500d);
		det.setDescuento(.4d);
		det.actualizar();
		p.agregarPartida(det);
		
		
		p=pedidoDao.save(p);
		flush();
		
		p=pedidoDao.get(p.getId());
		assertEquals("Pedido de Prueba", p.getComentario());
		assertNotNull(p.getId());
		assertFalse(p.getPartidas().isEmpty());
		assertNotNull(p.getPartidas().iterator().next().getId());
		System.out.println("PedidoDet.Id: "+p.getPartidas().iterator().next().getId());
		
		
		logger.info("Eliminando pedido: "+p.getId());
		pedidoDao.remove(p.getId());
		flush();
		
		try {
			p=pedidoDao.get(p.getId());
			fail("No debio encotrar el pedido: "+p.getId());
		} catch (ObjectRetrievalFailureException ore) {
			logger.debug("Expected exception: "+ore.getMessage());
			assertNotNull(ore);
		}
	}

	
	
	/**
	 * Verifica que el los registros de pendiente
	 * se persistan y/o eliminene con el pedido
	 * 
	 */
	@Test
	@Rollback(false)
	public void addRemovePendiente(){
		Pedido p=new Pedido();
		p.setCliente(testCliente);
		p.setFecha(new Date());
		p.setComentario("Pedido de Prueba");
		p.setFormaDePago(FormaDePago.EFECTIVO);
		
		p.setSucursal(sucursal);
		
		PedidoDet det=PedidoDet.getPedidoDet();
		Producto prod=productoDao.buscarPorClave("POL74");
		assertNotNull("Debe existir el producto: POL74",prod);
		det.setProducto(prod);
		BigDecimal precio=BigDecimal.valueOf(prod.getPrecioContado());
		det.setPrecio(precio);
		det.setPrecioLista(precio);
		det.setCantidad(500d);
		det.setDescuento(.4d);
		det.actualizar();
		p.agregarPartida(det);
		
		
		PedidoPendiente pendiente=new PedidoPendiente();
		pendiente.setComentario("PENDIENTE DE AUTORIZACION");
		
		p.setPendiente(pendiente);
		
		p=pedidoDao.save(p);
		flush();
		
		p=pedidoDao.get(p.getId());
		pendiente=p.getPendiente();
		assertNotNull(p.getPendiente());
		assertEquals("PENDIENTE DE AUTORIZACION", p.getPendiente().getComentario());
		
		logger.info("Eliminando pedido con pendiente");
		pedidoDao.remove(p.getId());
		flush();
		
		try {
			pendiente=(PedidoPendiente)universalDao.get(PedidoPendiente.class, pendiente.getId());
			fail("No debio encotrar el pendiente: "+pendiente.getId());
		} catch (ObjectRetrievalFailureException ore) {
			logger.debug("Expected exception: "+ore.getMessage());
			assertNotNull(ore);
		}
		
	}
	
	/**
	 * Test and left some data in the DB
	 * 
	 */
	@Test
	@Rollback(false)
	public void leftData(){
		//testCliente=clienteDao.buscarPorClave("U050008");
		//sucursal=sucursalDao.buscarPorClave(3);
		Pedido p=new Pedido();
		p.setCliente(testCliente);
		p.setFecha(new Date());
		p.setComentario("Pedido de Prueba");
		p.setFormaDePago(FormaDePago.EFECTIVO);
		
		p.setSucursal(sucursal);
		
		PedidoDet det=PedidoDet.getPedidoDet();
		Producto prod=productoDao.buscarPorClave("POL74");
		assertNotNull("Debe existir el producto: POL74",prod);
		det.setProducto(prod);
		BigDecimal precio=BigDecimal.valueOf(prod.getPrecioContado());
		det.setPrecio(precio);
		det.setPrecioLista(precio);
		det.setCantidad(500d);
		det.setDescuento(.4d);
		det.actualizar();
		p.agregarPartida(det);
		
		InstruccionDeEntrega ie=new InstruccionDeEntrega();
		ie.setCalle("Calle 1");
		ie.setColonia("Colonia 1");
		ie.setComentario("URGENTE");
		ie.setComentario2("URGENTE");
		ie.setCp("54040");
		ie.setEstado("MEXICO");
		ie.setMunicipio("LEON");
		ie.setNumero("254");
		p.setInstruccionDeEntrega(ie);
		
		p=pedidoDao.save(p);
		flush();
	}
}
