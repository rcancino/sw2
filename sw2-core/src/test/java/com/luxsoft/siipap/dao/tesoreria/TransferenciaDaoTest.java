package com.luxsoft.siipap.dao.tesoreria;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.Transferencia;

/**
 * Pruebas para la persistencia de Transferencias
 * asi como de las entidades asociadas 
 *  
 * @author Ruben Cancino
 *
 */
public class TransferenciaDaoTest extends BaseDaoTestCase{
	
	private TransferenciaDao transferenciaDao;
	private CargoAbonoDao cargoAbonoDao;
	private Sucursal sucursal;
	private Concepto c1;
	private Concepto c2;
	private Concepto c3;

	public void setTransferenciaDao(TransferenciaDao transferenciaDao) {
		this.transferenciaDao = transferenciaDao;
	}
	public void setCargoAbonoDao(CargoAbonoDao cargoAbonoDao) {
		this.cargoAbonoDao = cargoAbonoDao;
	}

	protected void onSetUp() throws Exception {	
		sucursal=(Sucursal)universalDao.get(Sucursal.class,new Long(1));
		assertNotNull(sucursal);
		c1=(Concepto)universalDao.get(Concepto.class, new Long(97));
		c2=(Concepto)universalDao.get(Concepto.class, new Long(98));
		c3=(Concepto)universalDao.get(Concepto.class, new Long(99));		
		assertNotNull(c1);
		assertNotNull(c2);
		assertNotNull(c3);
	}
	
	@Rollback(value=true)
	@SuppressWarnings("unused")
	public void testAddRemove(){
		Transferencia t=createTestBean();
		t.generarCargoAbono();
		t.generarCargoPorComision();
		t.registrarDatos(sucursal, c1, c2,c3,t.getAutorizacion());
		
		t=transferenciaDao.save(t);		
		flush();
		Long cargoId=t.getCargo().getId();
		Long abonoId=t.getAbono().getId();
		Long cargoComiId=t.getCargoComision().getId();
		assertNotNull(t.getId());		
		assertNotNull(cargoId);		
		assertNotNull(abonoId);
		assertNotNull(cargoComiId);
		System.out.println("Abono: "+ToStringBuilder.reflectionToString(t.getAbono(),ToStringStyle.MULTI_LINE_STYLE));
		
		transferenciaDao.remove(t.getId());
		
		flush();
		try {
			t=transferenciaDao.get(t.getId());			
			fail("No debe encontrar la transferencia");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("Eliminacion OK");
		}
		try {
			
			CargoAbono cargo=cargoAbonoDao.get(cargoId);			
			fail("Debe mandar error al no encontrar el cargo");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("Eliminacion OK");
		}
		try {
			CargoAbono abono=cargoAbonoDao.get(abonoId);			
			fail("Debe mandar error al no encontrar el abono");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("Eliminacion OK");
		}
		try {
			CargoAbono cargoCom=cargoAbonoDao.get(cargoId);			
			fail("Debe mandar error al no encontrar el cargo por comision");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("Eliminacion OK");
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	private Transferencia createTestBean(){
		final List<Cuenta> cuentas=universalDao.getAll(Cuenta.class);
		final Cuenta origen=cuentas.get(0);
		final Cuenta destino=cuentas.get(2);
		
		assertFalse(cuentas.isEmpty());
		assertTrue(cuentas.size()>1);
		
		final User user=(User)userDao.loadUserByUsername("admin");
		final Autorizacion aut=new Autorizacion(new Date(),user);
		aut.setComentario("Aut para transferencia:"+aut.getAutorizo().getUsername());
		
		BigDecimal importe=BigDecimal.valueOf(750000);
		
		
		Transferencia t=new Transferencia();
		t.setAutorizacion(aut);
		t.setOrigen(origen);
		t.setDestino(destino);
		t.setImporteOri(importe);
		t.setComision(BigDecimal.valueOf(50));
		t.setTc(BigDecimal.ONE);
		t.setComentario("Transferencia de prueba");
		return t;
	}
	

}
