package com.luxsoft.sw3.ventas.dao;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;

/**
 * Prueba de persistencia para la implementacion de para PedidoDao
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoDaoTest extends VentasBaseDaoTest{
	
	
	protected PedidoDao pedidoDao;
	
	protected ClienteDao clienteDao;
	
	protected SucursalDao sucursalDao;
	
	protected ProductoDao productoDao;
	
	private Cliente testCliente;
	
	private Sucursal sucursal;

	public PedidoDaoTest() {
		setPopulateProtectedVariables(true);
	}
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		testCliente=clienteDao.buscarPorClave("U050008");
		assertNotNull(testCliente);
		sucursal=sucursalDao.buscarPorClave(3);
		assertNotNull(sucursal);
	}

	
	
	public void testAddRemovePedido(){
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
		setComplete();
	}
	

}
