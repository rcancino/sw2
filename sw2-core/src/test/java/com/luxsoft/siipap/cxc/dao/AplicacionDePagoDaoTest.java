package com.luxsoft.siipap.cxc.dao;

import java.util.List;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDePago;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.ValidationUtils;
import com.luxsoft.siipap.ventas.dao.VentaDao;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Prueba de aplicacion de pagos
 * 
 * @author Ruben Cancino
 *
 */
public class AplicacionDePagoDaoTest extends BaseDaoTestCase{
	
	private PagoDao pagoDao;
	
	private ClienteDao clienteDao;
	
	private VentaDao ventaDao;
	
	Cliente cliente;
	List<Venta> ventas;
	
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		cliente=clienteDao.buscarPorClave("G011042");
		assertNotNull(cliente);
		ventas=ventaDao.buscarVentas(new Periodo("01/12/2008","31/12/2008"),cliente);
		assertFalse("Se requieren ventas para estas pruebas",ventas.isEmpty());
		System.out.println("Facturas: "+ventas.size());
	}

	public void testAddPagosVentasCredito(){
		
		Venta selected=ventas.get(0);
		
		PagoConCheque pago=new PagoConCheque();
		pago.setCliente(selected.getCliente());
		pago.setComentario("PRUEBA DE PAGO CON APLICACION");
		pago.setSucursal(selected.getSucursal());
		pago.setTotal(selected.getSaldo());
		pago.setNumero(58585);
		
		AplicacionDePago aplicacion=new AplicacionDePago();
		aplicacion.setComentario("PRUEBA DE APLICACION");
		aplicacion.setCargo(selected);
		aplicacion.setImporte(selected.getSaldo());
		pago.agregarAplicacion(aplicacion);
		
		//Salvar una aplicacion
		pago=(PagoConCheque)pagoDao.save(pago);
		flush();
		assertNotNull(pago.getId());
		
		pago=(PagoConCheque)pagoDao.get(pago.getId());		
		assertEquals(1,pago.getAplicaciones().size());
		
		
		//Eliminacion de aplicacion
		Aplicacion a=pago.getAplicaciones().get(0);
		pago.eliminarAplicacion(a);		
		pago=(PagoConCheque)pagoDao.save(pago);
		flush();
		
		pago=(PagoConCheque)pagoDao.get(pago.getId());	
		assertTrue(pago.getAplicaciones().isEmpty());
		assertEquals(pago.getTotal().doubleValue(), pago.getDisponibleCalculado().doubleValue());
		System.out.println("Pago por: "+pago.getTotal());
		System.out.println("Disponible: "+pago.getDisponibleCalculado());
		System.out.println("Aplicado: "+pago.getAplicado());		
	}
	
	public void testBulkAplicaciones(){
		final List<Venta> vts=ventaDao.buscarVentas(new Periodo("01/12/2008","31/12/2008"), OrigenDeOperacion.CRE);
		for(Venta selected:vts){
			if(selected.getCliente().equals(cliente))
				continue;
			if(selected.getSaldo().doubleValue()<=0)
				continue;
			PagoConCheque pago=new PagoConCheque();
			pago.setCliente(selected.getCliente());
			pago.setComentario("PRUEBA DE PAGO CON APLICACION");
			pago.setSucursal(selected.getSucursal());
			pago.setTotal(selected.getSaldo());
			pago.setNumero(58585);
			
			AplicacionDePago aplicacion=new AplicacionDePago();
			aplicacion.setComentario("PRUEBA DE APLICACION");
			aplicacion.setCargo(selected);
			aplicacion.setImporte(selected.getSaldo());
			pago.agregarAplicacion(aplicacion);
			
			//Salvar una aplicacion
			pago=(PagoConCheque)pagoDao.save(pago);
			ValidationUtils.debugValidation(pago);
		}
		setComplete();
	}
	
	public void setPagoDao(PagoDao pagoDao) {
		this.pagoDao = pagoDao;
	}
	public void setClienteDao(ClienteDao clienteDao) {
		this.clienteDao = clienteDao;
	}

	public void setVentaDao(VentaDao ventaDao) {
		this.ventaDao = ventaDao;
	}
	

}
