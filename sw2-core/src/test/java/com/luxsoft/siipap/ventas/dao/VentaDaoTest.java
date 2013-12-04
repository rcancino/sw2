package com.luxsoft.siipap.ventas.dao;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.ventas.model.EstadoDeVenta;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaCredito;
import com.luxsoft.siipap.ventas.model.VentaDet;


public class VentaDaoTest extends BaseDaoTestCase{
	
	VentaDao ventaDao;
	ProductoDao productoDao;
	
	public void testAddRemove(){
		
		Venta v=mockTest();
		v.setDocumento(52525l);
		v.setCliente(new Cliente("CX99","Cliente de prueba para Test de VentaDao"));
		v.setImporte(BigDecimal.valueOf(15000));
		v.setImpuesto(BigDecimal.valueOf(15000*.15));
		v.setTotal(BigDecimal.valueOf(15000*1.15));
		
		Long[] prods={502l,503l,504l};
		
		for(int i=0;i<prods.length;i++){
			VentaDet det=new VentaDet();
			Producto producto=productoDao.get(prods[i]);
			det.setProducto(producto);
			det.setCantidad(50);
			det.setPrecio(BigDecimal.valueOf(5000));
			det.setPrecioLista(BigDecimal.valueOf(5000*1.45));
			v.agregarPartida(det);
		}
		
		v=ventaDao.save(v);
		flush();
			
		assertNotNull(v.getId());
		
		ventaDao.remove(v.getId());		
		flush();
		
		try {
			v=ventaDao.get(v.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}		
	}
	
	public void testAddRemoveCredito(){
		Venta v=mockTest();
		v.setDocumento(54555l);
		VentaCredito credito=new VentaCredito();
		v.setComentario("Credito de prueba");
		v.setDiaPago(DateUtils.addDays(new Date(), 30));
		v.setReprogramarPago(v.getDiaPago());
		v.setFechaRevision(DateUtils.addDays(v.getDiaPago(), -5));
		v.setFechaRecepcionCXC(new Date());
		v.setFechaRevisionCxc(new Date());
		v.setFechaRevision(new Date());
		v.setVencimiento(DateUtils.addDays(new Date(), 30));
		
		v.setCredito(credito);
		//ValidationUtils.debugValidation(credito);
		v=ventaDao.save(v);
		flush();		
		credito=v.getCredito();
		assertNotNull(credito);
		
		//v.setCredito(null);
		//v=ventaDao.save(v);
		//flush();
		//assertNull(v.getCredito());
		/*
		try {
			//credito=(VentaCredito)universalDao.get(VentaCredito.class,credito.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
		*/
		v=ventaDao.get(v.getId());
		logger.info("OK Credito generado y eliminado exitosamente");
		
	}
	
	
	
	private  Venta mockTest(){
		Cliente cliente=new Cliente("CX01","Cliente de prueba para Test de VentaDao");
		Sucursal sucursal=(Sucursal)universalDao.get(Sucursal.class, new Long(2));
		
		Venta v=new Venta();
		v.setCliente(cliente);
		v.setComentario("Venta de prueba");
		v.setImporte(BigDecimal.valueOf(10000));
		v.setImpuesto(v.getImporte().multiply(BigDecimal.valueOf(.15)));
		v.setTotal(BigDecimal.valueOf(10000*1.15));
		v.setOrigen(OrigenDeOperacion.CRE);
		v.setSaldo(v.getTotal());
		v.setSucursal(sucursal);
		return v;
	}



	public void setVentaDao(VentaDao ventaDao) {
		this.ventaDao = ventaDao;
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}
	
	
	

}
