package com.luxsoft.siipap.cxp.dao;

import java.math.BigDecimal;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.compras.dao.ProveedorDao;
import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.core.Proveedor;

public class ContraReciboDaoTest extends BaseDaoTestCase{
	
	private ContraReciboDao contraReciboDao;
	
	private ProveedorDao proveedorDao;
	
	private Proveedor proveedor;
	
	
	
	/*@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		this.deleteFromTables(
				new String[]{"sx_cxp_recibos_det"
						,"sx_cxp_recibos"});
	}*/

	@Override
	protected void onSetUpInTransaction() throws Exception {
		proveedor=proveedorDao.buscarPorClave("XXP1");
		if(proveedor==null){
			proveedor=new Proveedor("Proveedor Test "+System.currentTimeMillis());
			proveedor.setClave("XXP1");
			proveedor=proveedorDao.save(proveedor);
		}
	}

	public void testAddRemove(){
		assertNotNull(proveedor);
		
		ContraRecibo recibo=new ContraRecibo();
		recibo.setProveedor(proveedor);		
		recibo.setTotal(BigDecimal.valueOf(500));
		recibo.setComentario("PRUEBA DE PERSISTENCIA");
		for(int i=0;i<10; i++){
			ContraReciboDet det=new ContraReciboDet();
			det.setDocumento(String.valueOf(i));
			det.setTotal(BigDecimal.valueOf(50));
			recibo.agregarPartida(det);
		}
		recibo=contraReciboDao.save(recibo);
		flush();
		assertNotNull(recibo.getId());
		for(ContraReciboDet det:recibo.getPartidas()){
			assertNotNull(det.getId());
		}
		//Delete
		contraReciboDao.remove(recibo.getId());
		flush();
		try {
			contraReciboDao.get(recibo.getId());
			fail("No debe existir la instancia");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
		setComplete();
	}
	
	public void setContraReciboDao(ContraReciboDao contraReciboDao) {
		this.contraReciboDao = contraReciboDao;
	}
	public void setProveedorDao(ProveedorDao proveedorDao) {
		this.proveedorDao = proveedorDao;
	}

}
