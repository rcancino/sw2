package com.luxsoft.sw3.tesoreria.dao;


import java.util.Date;

import org.springframework.dao.DataAccessException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;

public class CorteDeTarjetaDaoTest extends BaseDaoTestCase{
	
	private CorteDeTarjetaDao corteDeCajaDao;
	
	Sucursal sucursal;
	Cuenta cuenta;


	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		sucursal=(Sucursal)universalDao.get(Sucursal.class, new Long(3));
		assertNotNull(sucursal);
		
		cuenta=(Cuenta)universalDao.get(Cuenta.class, new Long(151212));
		assertNotNull(cuenta);
	}

	public void testAddRemove(){
		
		CorteDeTarjeta corte=new CorteDeTarjeta();
		corte.setSucursal(sucursal);
		corte.setCuenta(cuenta);
		corte.setFecha(new Date());
		corte.setComentario("CORTE DE PRUEBA");
		corte=corteDeCajaDao.save(corte);
		flush();
		assertNotNull(corte);
		corte=corteDeCajaDao.get(corte.getId());
		assertEquals("CORTE DE PRUEBA", corte.getComentario());
		
		//setComplete();
		
		log.debug("Eliminando corte");
		corteDeCajaDao.remove(corte.getId());
		flush();
		
		try {
			corteDeCajaDao.get(corte.getId());
			fail("Corte de Tarjeta encontrado en la base de datos");
		} catch (DataAccessException e) {
			log.debug("Expected exception: "+e.getMessage());
			assertNotNull(e);
		}
		
	}

	public void setCorteDeCajaDao(CorteDeTarjetaDao corteDeCajaDao) {
		this.corteDeCajaDao = corteDeCajaDao;
	}
	

}
