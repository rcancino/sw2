package com.luxsoft.siipap.dao.tesoreria;

import java.util.List;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Conciliacion;

public class ConciliacionDaoTest extends BaseDaoTestCase{
	
	private ConciliacionDao conciliacionDao;

	public void setConciliacionDao(ConciliacionDao conciliacionDao) {
		this.conciliacionDao = conciliacionDao;
	}
	
	@SuppressWarnings("unchecked")
	public void testAddRemove(){
		List<CargoAbono> cargos=universalDao.getAll(CargoAbono.class);
		assertFalse(cargos.isEmpty());
		for(int index=0;index<cargos.size()/2;index++){
			CargoAbono b=cargos.get(index);
			Conciliacion c=new Conciliacion(b);
			c.setComentario("Test Concil");
			c.setFecha(b.getFecha());
			//c.setImporteBanco(b.getUncriptedImporte());
			System.out.println("Importe: "+b.getImporte());
			c.setReferencia(b.getReferencia());
			c=conciliacionDao.save(c);
			assertNotNull(c.getId());
		}
		setComplete();
	}

}
