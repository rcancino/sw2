package com.luxsoft.siipap.cxc.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.Cuenta;

/**
 * Pruebas para la persistencia de Fichas de depositos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FichaDaoTest extends BaseDaoTestCase{
	
	private Sucursal sucursal;	
	private List<Pago> pagos=new ArrayList<Pago>();
	private Cuenta cuenta;
	
	private SucursalDao sucursalDao;
	
	private PagoDao pagoDao;
	
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		sucursal=sucursalDao.buscarPorClave(1);
		cuenta=(Cuenta)universalDao.getAll(Cuenta.class).iterator().next();
		List<Pago> data=pagoDao.buscarPagosDisponibles(new Cliente("I020376",""));
		assertFalse("No exsisten pagos para las pruebas",data.isEmpty());
		assertTrue("Los pagos para las pruebas no son suficientes",data.size()>=5);
		pagos.addAll(data.subList(0, 4));
	}

	public void testAddRemove(){
		assertNotNull(sucursal);
		assertNotNull(cuenta);
		Ficha f=new Ficha();
		f.setSucursal(sucursal);
		f.setCuenta(cuenta);
		f.setComentario("Ficha de prueba");
		for(Pago pago:pagos){
			FichaDet det=new FichaDet();
			det.setBanco("BANCO");
			det.setPago(pago);
			pago.setDeposito(det);
			f.getPartidas().add(det);
		}
		f=(Ficha) universalDao.save(f);
		flush();
		assertNotNull(f.getId());
		for(FichaDet det:f.getPartidas()){
			assertNotNull(det.getId());
		}
		//Remove
		universalDao.remove(Ficha.class, f.getId());
		flush();
		try {
			universalDao.get(Ficha.class, f.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException ore) {
			assertNotNull(ore);
			logger.info("OK");
		}
		
	}

	public void setSucursalDao(SucursalDao sucursalDao) {
		this.sucursalDao = sucursalDao;
	}

	
	public void setPagoDao(PagoDao pagoDao) {
		this.pagoDao = pagoDao;
	}
	
	

}
