package com.luxsoft.siipap.dao.tesoreria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;


public class CargoAbonoDaoTest extends BaseDaoTestCase{
	
	private CargoAbonoDao cargoAbonoDao;	
	

	public void setCargoAbonoDao(CargoAbonoDao cargoAbonoDao) {
		this.cargoAbonoDao = cargoAbonoDao;
	}
	
	
	
	public void testCollaborators(){
		assertNotNull(cargoAbonoDao);
		assertNotNull(userDao);
	}
	
	
	public void testAddCargos(){		
		List<CargoAbono> cargos=crearBeansCargoAbono(true);
		for(int i=0;i<cargos.size();i++){
			CargoAbono res=cargoAbonoDao.save(cargos.get(i));
			assertNotNull(res.getId());
		}
		setComplete();
	}
	
	public void testAddAbonos(){		
		List<CargoAbono> abonos=crearBeansCargoAbono(false);
		for(int i=0;i<abonos.size();i++){
			CargoAbono res=cargoAbonoDao.save(abonos.get(i));
			assertNotNull(res.getId());
		}
		setComplete();
	}
	
	@SuppressWarnings("unchecked")
	private List<CargoAbono> crearBeansCargoAbono(boolean isCargo){
		final List<Cuenta> cuentas=universalDao.getAll(Cuenta.class);
		final List<Sucursal> sucursales=universalDao.getAll(Sucursal.class);
		assertFalse(cuentas.isEmpty());
		
		final User user=(User)userDao.loadUserByUsername("admin");
		assertNotNull(user);
		final Double[] importes={500d,650d,800d,350d,740d};
		final Periodo periodo=new Periodo("01/12/2007","31/01/2008");
		final String aFavor="Sistemas de Especiales S.A.";
		final List<CargoAbono> beans=new ArrayList<CargoAbono>();
		final UserLog log=new UserLog(user,user);
		for(Date fecha :periodo.getListaDeDias()){
			
			int nextImporte=RandomUtils.nextInt(importes.length-1);
			final BigDecimal importe=BigDecimal.valueOf(importes[nextImporte]);
			int nextCta=RandomUtils.nextInt(cuentas.size()-1);
			final Cuenta cta=cuentas.get(nextCta);
			int  nextSuc=RandomUtils.nextInt(sucursales.size()-1);
			final Autorizacion aut=new Autorizacion(fecha,user);
			aut.setComentario("Aut test:"+aut.getAutorizo().getUsername());
			
			if(isCargo){
				final Concepto tipo=(Concepto)universalDao.get(Concepto.class, new Long(2));
				assertNotNull(tipo);
				CargoAbono cargo=CargoAbono.crearCargo(cta, importe, fecha, aFavor, tipo,sucursales.get(nextSuc));
				cargo.setAutorizacion(aut);
				cargo.setComentario("Cargo de prueba");
				cargo.setUserLog(log);
				beans.add(cargo);
			}else{
				final Concepto tipo=(Concepto)universalDao.get(Concepto.class, new Long(1));
				assertNotNull(tipo);
				CargoAbono abono=CargoAbono.crearAbono(cta, importe, fecha, tipo,sucursales.get(nextSuc));
				abono.setAutorizacion(aut);
				abono.setComentario("Abono de prueba");
				abono.setUserLog(log);
				beans.add(abono);
			}
			
			
		}
		return beans;
	}
	

}
