package com.luxsoft.siipap.dao.gastos;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.tesoreria.RequisicionDao;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class GFacturasPorCompraDaoTest extends BaseDaoTestCase{
	
	RequisicionDao requisicionDao;
	
	/*
	public void testRequisicion(){
		//GCompra compra=compraDao.getAll().get(0);
		GFacturaPorCompra fac=(GFacturaPorCompra)
			universalDao.getAll(GFacturaPorCompra.class).get(0);
		
		assertNull(fac.getRequisiciondet());
		Requisicion req=RequisicionesUtils.generarRequisicion(fac);
		assertNotNull(req);
		req=requisicionDao.save(req);		
		flush();
		assertNotNull(req.getId());
		setComplete();
		
		
	}
*/
	/**
	 * @param requisicionDao the requisicionDao to set
	 */
	public void setRequisicionDao(RequisicionDao requisicionDao) {
		this.requisicionDao = requisicionDao;
	}

	

}
