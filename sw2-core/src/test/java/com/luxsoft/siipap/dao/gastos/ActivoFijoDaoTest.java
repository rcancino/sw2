package com.luxsoft.siipap.dao.gastos;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.gastos.ActivoFijo;
import com.luxsoft.siipap.model.gastos.ClasificacionDeActivo;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.INPC;

public class ActivoFijoDaoTest extends BaseDaoTestCase{
	
	private ActivoFijoDao activoFijoDao;
	
	public void testAddRemoveActivo(){
		ClasificacionDeActivo c=new ClasificacionDeActivo();
		c.setNombre("MI ACTIVO");
		c.setTasa(20.00);
		
		ConceptoDeGasto rubro=(ConceptoDeGasto)universalDao.getAll(ConceptoDeGasto.class).get(0);
		assertNotNull("Debe existir por lo menos un ConceptoDeGasto en la B.D",rubro);
		
		GProductoServicio p=new GProductoServicio();
		p.setClave("ACTIVO_1");
		p.setDescripcion("ACTIVO DE PRUEBA 1");
		p.setInversion(true);
		p.setRubro(rubro);
		
		ActivoFijo af=new ActivoFijo();
		af.setClasificacion(c);
		af.setFechaDeAdquisicion(new Date());
		af.setInpc(3.5);
		af.setMoi(BigDecimal.valueOf(75000));
		af.setProducto(p);
		af.setTasaDepreciacion(c.getTasa());
		
		INPC inpc=new INPC(2008,3,2.25);
		af.setInpcOriginal(inpc);
		
		af=activoFijoDao.save(af);
		flush();
		assertNotNull(af.getId());
		
		activoFijoDao.remove(af.getId());
		flush();
		try {
			af=activoFijoDao.get(af.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
			logger.debug("ERROR OK");
		}
		
	}

	public void setActivoFijoDao(ActivoFijoDao activoFijoDao) {
		this.activoFijoDao = activoFijoDao;
	}
	
	

}
