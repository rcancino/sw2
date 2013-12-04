package com.luxsoft.siipap.cxc.dao;

import java.math.BigDecimal;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.util.ValidationUtils;

/**
 * Prueba la persistencia de pagos mediante la implementacion de {@link PagoDao}
 * 
 * @author Ruben Cancino
 *
 */
public class PagoDaoTest extends BaseDaoTestCase{
	
	private PagoDao pagoDao;
	
	private Cliente[] clientes=new Cliente[3];
	private Sucursal sucursal;
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		clientes[0]=(Cliente)universalDao.save(new Cliente("CXCT_1","Cliente de prueba Cargo pagos "+1));
		clientes[1]=(Cliente)universalDao.save(new Cliente("CXCT_2","Cliente de prueba Cargo pagos "+2));
		clientes[2]=(Cliente)universalDao.save(new Cliente("CXCT_3","Cliente de prueba Cargo pagos "+3));
		sucursal=(Sucursal)universalDao.get(Sucursal.class, new Long(1));
	}

	public void testAddPagoConEfectivo(){
		PagoConEfectivo pago=new PagoConEfectivo();
		pago.setCliente(clientes[0]);
		pago.setComentario("PRUEBA DE PAGO EN EFECTIVO");
		pago.setSucursal(sucursal);
		pago.setTotal(BigDecimal.valueOf(50000));
		
		pago=(PagoConEfectivo)pagoDao.save(pago);
		flush();
		assertNotNull(pago.getId());
		
		pagoDao.remove(pago.getId());
		flush();
		try {
			pagoDao.get(pago.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}
	
	
	public void testAddPagoConCheque(){
		PagoConCheque pago=new PagoConCheque();
		pago.setCliente(clientes[1]);
		pago.setComentario("PRUEBA DE PAGO CON CHEQUE");
		pago.setSucursal(sucursal);
		pago.setTotal(BigDecimal.valueOf(50000));
		pago.setNumero(58585);
		pago=(PagoConCheque)pagoDao.save(pago);
		flush();
		assertNotNull(pago.getId());
		
		pagoDao.remove(pago.getId());
		flush();
		try {
			pagoDao.get(pago.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}
	
	public void testAddPagoConTarjeta(){
		PagoConTarjeta pago=new PagoConTarjeta();
		pago.setCliente(clientes[1]);
		pago.setComentario("PRUEBA DE PAGO CON TARJETA");
		pago.setSucursal(sucursal);
		pago.setTotal(BigDecimal.valueOf(50000));
		Tarjeta tarjeta=(Tarjeta)universalDao.get(Tarjeta.class, new Long(5));
		pago.setTarjeta(tarjeta);
		pago.setAutorizacionBancaria("TEST AUT");
		pago=(PagoConTarjeta)pagoDao.save(pago);
		flush();
		assertNotNull(pago.getId());
		
		pagoDao.remove(pago.getId());
		flush();
		try {
			pagoDao.get(pago.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}
	
	public void testAddPagoConDeposito(){
		PagoConDeposito pago=new PagoConDeposito();
		pago.setCliente(clientes[2]);
		pago.setComentario("PRUEBA DE PAGO CON DEPOSITO");
		pago.setSucursal(sucursal);
		pago.setTransferencia(BigDecimal.valueOf(55000));
		Cuenta cuenta=(Cuenta)universalDao.get(Cuenta.class, new Long(151212L));
		pago.setCuenta(cuenta);
		
		pago.setReferenciaBancaria("TEST_REF");
		pago=(PagoConDeposito)pagoDao.save(pago);
		flush();
		assertNotNull(pago.getId());
		
		pagoDao.remove(pago.getId());
		flush();
		try {
			pagoDao.get(pago.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
	}
	
	/**
	 * Util para otros test que requieran datos de pagos
	 * 
	 */
	public void testAddBulkData(){
		
		Cliente cliente=clienteDao.buscarPorClave("U050008");
		Cliente cliente2=clienteDao.buscarPorClave("I020376");
		
		// Depositos en efectivo
		for(int i=0;i<20;i++){
			PagoConEfectivo pago=new PagoConEfectivo();
			pago.setCliente(cliente);
			pago.setComentario("PRUEBA DE PAGO EN EFECTIVO");
			pago.setSucursal(sucursal);
			pago.setTotal(BigDecimal.valueOf(50000));			
			pago=(PagoConEfectivo)pagoDao.save(pago);
		}		
		//Con tarjeta
		/*Tarjeta tarjeta=(Tarjeta)universalDao.get(Tarjeta.class, new Long(5));
		for(int i=0;i<20;i++){
			PagoConTarjeta pago=new PagoConTarjeta();
			pago.setCliente(cliente);
			pago.setComentario("PRUEBA DE PAGO CON TARJETA");
			pago.setSucursal(sucursal);
			pago.setTotal(BigDecimal.valueOf(70000));
			pago.setTarjeta(tarjeta);
			pago.setAutorizacionBancaria("TEST AUT");
			pago=(PagoConTarjeta)pagoDao.save(pago);
		}*/
		//Con Cheque
		for(int i=0;i<20;i++){
			PagoConCheque pago=new PagoConCheque();
			pago.setCliente(cliente2);
			pago.setComentario("PRUEBA DE PAGO CON CHEQUE");
			pago.setSucursal(sucursal);
			pago.setTotal(BigDecimal.valueOf(50000));
			pago.setNumero(58585+i);
			pago.setBanco("BANAMEX");
			ValidationUtils.debugValidation(pago);
			pago=(PagoConCheque)pagoDao.save(pago);			
		}		
		//Con Transferencia
		Cuenta cuenta=(Cuenta)universalDao.get(Cuenta.class, new Long(151212L));
		for(int i=0;i<10;i++){
			PagoConDeposito pago=new PagoConDeposito();
			pago.setCliente(cliente);
			pago.setComentario("PRUEBA DE PAGO CON TRANSFERENCIA");
			pago.setSucursal(sucursal);
			pago.setTransferencia(BigDecimal.valueOf(50000));
			pago.setCuenta(cuenta);
			pago.setReferenciaBancaria("TEST_REF");
			pago=(PagoConDeposito)pagoDao.save(pago);
		}		
		//Con deposito
		for(int i=0;i<10;i++){
			PagoConDeposito pago=new PagoConDeposito();
			pago.setCliente(cliente2);
			pago.setComentario("PRUEBA DE PAGO CON DEPOSITO");
			pago.setSucursal(sucursal);
			pago.setCheque(BigDecimal.valueOf(50000));
			pago.setEfectivo(BigDecimal.valueOf(25000));
			pago.setCuenta(cuenta);
			pago.setReferenciaBancaria("TEST_REF");
			pago=(PagoConDeposito)pagoDao.save(pago);
		}
		setComplete();
	}
	

	public void setPagoDao(PagoDao pagoDao) {
		this.pagoDao = pagoDao;
	}
	
	@SuppressWarnings("unused")
	private ClienteDao clienteDao;

	public void setClienteDao(ClienteDao clienteDao) {
		this.clienteDao = clienteDao;
	}
	
	
	

}
