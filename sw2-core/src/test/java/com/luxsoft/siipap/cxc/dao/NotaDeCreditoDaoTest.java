package com.luxsoft.siipap.cxc.dao;

import java.math.BigDecimal;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.Concepto;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento.TipoDeDescuento;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.ValidationUtils;

/**
 * Prueba la persistencia de Notas de Credito
 * 
 * @author Ruben Cancino
 *
 */
public class NotaDeCreditoDaoTest extends BaseDaoTestCase{
	
	private Cliente[] clientes=new Cliente[3];
	private Sucursal sucursal;
	private NotaDeCreditoDao notaDeCreditoDao;
	
	private ClienteDao clienteDao;
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		clientes[0]=clienteDao.save(new Cliente("CXCNC_1","Cliente de prueba Cargo nota credito_"+1));
		clientes[1]=clienteDao.save(new Cliente("CXCNC_2","Cliente de prueba Cargo nota credito_"+2));
		clientes[2]=clienteDao.save(new Cliente("CXCNC_3","Cliente de prueba Cargo nota credito_"+3));
		sucursal=(Sucursal)universalDao.get(Sucursal.class, new Long(1));
	}
	
	/**
	 * Prueba el Alta y baja de notas por bonificacion
	 * 
	 */
	public void testNotaBonoficacion(){
		NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
		nota.setSucursal(sucursal);
		nota.setCliente(clientes[0]);
		nota.setConcepto(Concepto.BONIFICACION);
		nota.setComentario("Prueba de nota de credito por bonoficacion");
		nota.setFolio(50);
		nota.setDescuento(.05);
		nota.setImporte(BigDecimal.valueOf(20000));
		nota.actualizarImpuesto();
		ValidationUtils.debugValidation(nota);
		nota=(NotaDeCreditoBonificacion)notaDeCreditoDao.save(nota);
		flush();
		assertNotNull(nota.getId());
		
		notaDeCreditoDao.remove(nota.getId());
		flush();
		try {
			notaDeCreditoDao.get(nota.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}
	
	public void testNotaDescuento(){
		NotaDeCreditoDescuento nota=new NotaDeCreditoDescuento();
		nota.setSucursal(sucursal);
		nota.setCliente(clientes[0]);
		nota.setTipoDeDescuento(TipoDeDescuento.ASIGNADO);
		nota.setComentario("Prueba de nota de credito por bonoficacion");
		nota.setFolio(44);
		nota.setDescuento(.05);
		nota.setImporte(BigDecimal.valueOf(20000));
		nota.actualizarImpuesto();
		if(!ValidationUtils.isValid(nota))
			ValidationUtils.debugValidation(nota);
		nota=(NotaDeCreditoDescuento)notaDeCreditoDao.save(nota);
		flush();
		assertNotNull(nota.getId());
		
		notaDeCreditoDao.remove(nota.getId());
		flush();
		try {
			notaDeCreditoDao.get(nota.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}
	
	public void testBulkData(){
		
		Cliente cliente=clienteDao.buscarPorClave("U050008");
		Cliente cliente2=cliente;
		assertNotNull(cliente);
		assertNotNull(cliente2);
		//Bonificacion
		for(int i=0;i<20;i++){
			NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
			nota.setSucursal(sucursal);
			nota.setCliente(cliente);
			nota.setConcepto(Concepto.BONIFICACION);
			nota.setComentario("Prueba de nota de credito por bonoficacion");
			nota.setFolio(90+i);
			nota.setDescuento(.05);
			nota.setImporte(BigDecimal.valueOf(20000));
			nota.actualizarImpuesto();
			nota=(NotaDeCreditoBonificacion)notaDeCreditoDao.save(nota);
			flush();
		}
		
		//Descuento
		for(int i=0;i<20;i++){
			NotaDeCreditoDescuento nota=new NotaDeCreditoDescuento();
			nota.setSucursal(sucursal);
			nota.setCliente(cliente2);
			nota.setTipoDeDescuento(TipoDeDescuento.ASIGNADO);
			nota.setComentario("Prueba de nota de credito por bonoficacion");
			nota.setFolio(50+i);
			nota.setDescuento(.05);
			nota.setImporte(BigDecimal.valueOf(20000));
			nota.actualizarImpuesto();
			nota=(NotaDeCreditoDescuento)notaDeCreditoDao.save(nota);
		}
		setComplete();
	}
	
	public void setClienteDao(ClienteDao clienteDao) {
		this.clienteDao = clienteDao;
	}

	public void setNotaDeCreditoDao(NotaDeCreditoDao notaDao) {
		this.notaDeCreditoDao = notaDao;
	}
	
	

}
