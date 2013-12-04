package com.luxsoft.siipap.ventas.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;

/**
 * Prueba general del Dao par {@link ListaDePreciosCliente}
 * 
 * @author Ruben Cancino
 *
 */
public class ListaDePreciosClienteDaoTest extends BaseDaoTestCase{
	
	private ListaDePreciosClienteDao listaDePreciosClienteDao;
	private ClienteDao clienteDao;
	private Cliente cliente;
	private ProductoDao productoDao;
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		cliente=new Cliente("XX01","Cliente de prueba de descuentos");
		cliente=clienteDao.save(cliente);
	}
	
	public void testAddRemove(){
		ListaDePreciosCliente lp=new ListaDePreciosCliente();
		lp.setCliente(cliente);
		lp.setActivo(true);
		lp.setComentario("Lista de precios de prueba");		
		lp.setFechaInicial(new Date());
		lp.setFechaFinal(DateUtils.addDays(new Date(),20));
		lp=listaDePreciosClienteDao.save(lp);
		for(int i=0;i<5;i++){
			ListaDePreciosClienteDet det=new ListaDePreciosClienteDet();
			Producto producto=productoDao.get(new Long(502+i));
			det.setProducto(producto);
			det.setPrecio(500);
			lp.agregarPrecio(det);
		}
		
		flush();
		assertNotNull(lp.getId());
		
		listaDePreciosClienteDao.remove(lp.getId());
		flush();
		try {
			listaDePreciosClienteDao.get(lp.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
		
	}
	
	/**
	 * Util patra generar listas de precios de prueba
	 * 
	
	public void testBulkData(){
		
		List<Producto> productos=productoDao.buscarActivos();
		
		for(int i=0;i<5;i++){
			
			ListaDePreciosCliente lp=new ListaDePreciosCliente();
			lp.setCliente(cliente);
			lp.setActivo(true);
			lp.setComentario("Lista de precios de prueba: "+i);		
			lp.setFechaInicial(new Date());
			lp.setFechaFinal(DateUtils.addDays(new Date(),20));
			for(Producto producto:productos){
				ListaDePreciosClienteDet det=new ListaDePreciosClienteDet();
				det.setProducto(producto);
				BigDecimal precio=BigDecimal.valueOf(producto.getPrecioCredito());
				precio=precio.multiply(BigDecimal.valueOf(.97));
				det.setPrecio(precio);
				lp.agregarPrecio(det);
			}
			lp=listaDePreciosClienteDao.save(lp);
		}
		setComplete();
	}
 */
	public void setListaDePreciosClienteDao(
			ListaDePreciosClienteDao listaDePreciosClienteDao) {
		this.listaDePreciosClienteDao = listaDePreciosClienteDao;
	}

	public void setClienteDao(ClienteDao clienteDao) {
		this.clienteDao = clienteDao;
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}
	
	

}
