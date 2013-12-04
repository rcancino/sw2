package com.luxsoft.siipap.cxc.dao;

import java.util.List;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.cxc.model.AbstractNotasRules;
import com.luxsoft.siipap.cxc.model.NotaRules;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.Concepto;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento.TipoDeDescuento;
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
public class AplicacionDeNotasDaoTest extends BaseDaoTestCase{
	
	private NotaDeCreditoDao notaDeCreditoDao;
	
	private ClienteDao clienteDao;
	
	private VentaDao ventaDao;
	
	final NotaRules rules=new AbstractNotasRules();
	
	Cliente cliente;
	List<Venta> ventas;
	
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		cliente=clienteDao.buscarPorClave("I020376");
		assertNotNull(cliente);
		ventas=ventaDao.buscarVentas(new Periodo("01/01/2008","31/12/2008"),cliente);
		assertFalse("Se requieren ventas para estas pruebas",ventas.isEmpty());
		System.out.println("Facturas: "+ventas.size());
	}

	public void testAddPagosVentasCredito(){
		
		Venta selected=ventas.get(0);
		
		NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
		nota.setCliente(selected.getCliente());
		nota.setComentario("PRUEBA DE NOTA CON APLICACION");
		nota.setConcepto(Concepto.BONIFICACION);
		nota.setSucursal(selected.getSucursal());
		nota.setFolio(45);
		nota.setDescuento(.05);
		
		
		
		AplicacionDeNota aplicacion=new AplicacionDeNota();
		aplicacion.setComentario("APLICACION de NOTA DE PRUEBA");
		aplicacion.setCargo(selected);
		nota.agregarAplicacion(aplicacion);
		
		//Aplicamos el descuento de la nota
		rules.aplicarDescuento(nota);
		
		//Actualizamos los totales de la nota
		rules.actualizarImportesDesdeAplicaciones(nota);
		nota.actualizarImpuesto();
		
		//Salvar la nota con la aplicacion
		nota=(NotaDeCreditoBonificacion)notaDeCreditoDao.save(nota);
		flush();
		assertNotNull(nota.getId());
		
		nota=(NotaDeCreditoBonificacion)notaDeCreditoDao.get(nota.getId());		
		assertEquals(1,nota.getAplicaciones().size());
		
		
		//Eliminacion de aplicacion
		Aplicacion a=nota.getAplicaciones().get(0);
		nota.eliminarAplicacion(a);		
		nota=(NotaDeCreditoBonificacion)notaDeCreditoDao.save(nota);
		flush();
		
		nota=(NotaDeCreditoBonificacion)notaDeCreditoDao.get(nota.getId());	
		assertTrue(nota.getAplicaciones().isEmpty());
		assertEquals(nota.getTotal().doubleValue(), nota.getDisponibleCalculado().doubleValue());
		System.out.println("Nota por: "+nota.getTotal());
		System.out.println("Disponible: "+nota.getDisponibleCalculado());
		System.out.println("Aplicado: "+nota.getAplicado());		
	}
	
	
	public void testBulkAplicaciones(){
		final List<Venta> vts=ventaDao.buscarVentas(new Periodo("22/12/2008","22/12/2008"), OrigenDeOperacion.CRE);
		
		for (int i=0;i<vts.size();i++){
			Venta selected=vts.get(i);
			if(selected.getCliente().equals(cliente))
				continue;
			if(selected.getSaldo().doubleValue()<=0)
				continue;
			if(i%2==0){
				//De Bonificacion
				System.out.println("BONIFICACION");

				NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
				nota.setCliente(selected.getCliente());
				nota.setComentario("PRUEBA DE NOTA CON APLICACION");
				nota.setConcepto(Concepto.BONIFICACION);
				nota.setSucursal(selected.getSucursal());
				nota.setFolio(95+i);
				nota.setDescuento(.05);
				
				
				
				AplicacionDeNota aplicacion=new AplicacionDeNota();
				aplicacion.setComentario("APLICACION de NOTA DE PRUEBA");
				aplicacion.setCargo(selected);
				nota.agregarAplicacion(aplicacion);
				
				//Aplicamos el descuento de la nota
				rules.aplicarDescuento(nota);
				
				//Actualizamos los totales de la nota
				rules.actualizarImportesDesdeAplicaciones(nota);
				
				
				//Salvar la nota con la aplicacion
				ValidationUtils.debugValidation(nota);
				nota=(NotaDeCreditoBonificacion)notaDeCreditoDao.save(nota);
				flush();
			}else{
				System.out.println("DESCUENTO");

				NotaDeCreditoDescuento nota=new NotaDeCreditoDescuento();
				nota.setCliente(selected.getCliente());
				nota.setComentario("PRUEBA DE NOTA CON APLICACION");
				nota.setTipoDeDescuento(TipoDeDescuento.ASIGNADO);
				nota.setSucursal(selected.getSucursal());
				nota.setFolio(45+i);
				nota.setDescuento(.05);
				
				
				
				AplicacionDeNota aplicacion=new AplicacionDeNota();
				aplicacion.setComentario("APLICACION de NOTA DE PRUEBA");
				aplicacion.setCargo(selected);
				nota.agregarAplicacion(aplicacion);
				
				//Aplicamos el descuento de la nota				
				rules.aplicarDescuento(nota);
				
				//Actualizamos los totales de la nota
				rules.actualizarImportesDesdeAplicaciones(nota);
				
				
				//Salvar la nota con la aplicacion
				nota=(NotaDeCreditoDescuento)notaDeCreditoDao.save(nota);
				ValidationUtils.debugValidation(nota);
				flush();
			}			
		}
		setComplete();
	}
	
	
	public void setNotaDeCreditoDao(NotaDeCreditoDao notaDeCreditoDao) {
		this.notaDeCreditoDao = notaDeCreditoDao;
	}

	public void setClienteDao(ClienteDao clienteDao) {
		this.clienteDao = clienteDao;
	}

	public void setVentaDao(VentaDao ventaDao) {
		this.ventaDao = ventaDao;
	}
	

}
