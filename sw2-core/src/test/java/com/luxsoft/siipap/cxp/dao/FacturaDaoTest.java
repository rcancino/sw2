package com.luxsoft.siipap.cxp.dao;

import java.math.BigDecimal;

import org.springframework.dao.DataAccessException;

import com.luxsoft.siipap.compras.dao.EntradaPorCompraDao;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.core.Proveedor;

public class FacturaDaoTest extends BaseDaoTestCase{
	
	private FacturaDao facturaDao;
	
	private Proveedor proveedor;
	
	
	
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		proveedor=new Proveedor("Prov para analisis");
		proveedor.setClave("PVAN");
		proveedor=(Proveedor)universalDao.save(proveedor);
	}
	

	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		universalDao.remove(Proveedor.class, proveedor.getId());
	}


	/**
	 * Probamos agregar y elminar una CXPFactura sin partidas
	 * 
	 */
	public void testAddRemove(){
		CXPFactura fac=new CXPFactura();
		fac.setProveedor(proveedor);
		fac.setDocumento("2535");
		fac.setComentario("CXPFactura de prueba");
		fac.setTotal(BigDecimal.valueOf(23000.00));
		fac.actualizarVencimiento();
		
		fac=facturaDao.save(fac);
		flush();
		assertNotNull(fac.getId());
		logger.info("CXPFactura registrada:" +fac);
		//setComplete();
		
		fac=facturaDao.get(fac.getId());
		assertEquals( 23000.00d, fac.getTotal().doubleValue());
		assertEquals(23000.00d, fac.getSaldo().doubleValue());
		
		facturaDao.remove(fac.getId());
		flush();
		
		try {
			facturaDao.get(fac.getId());
			fail("Debio mandar error al no encontrar el objeto");
		} catch (DataAccessException e) {
			assertNotNull(e);
		}		
	}
	
	/**
	 * Probamos agregar y que el costo unitario se persista en el invenrario
	 * 
	 */
	public void testAddRemoveAnalisisDeEntradas(){
		
		/*CXPFactura fac=new CXPFactura();
		fac.setProveedor(proveedor);
		fac.setDocumento("2535");
		fac.setComentario("CXPFactura de prueba");
		fac.setTotal(BigDecimal.valueOf(23000));
		
		EntradaPorCompra e=entradaPorCompraDao.get(11L);
		System.out.println("Entrada encontrada: "+e);
		CXPAnalisisDet det=new CXPAnalisisDet();
		det.setEntrada(e);
		det.setCantidad(e.getPorAnalizar());
		det.setPrecio(BigDecimal.valueOf(50.55));
		fac.agregarPartida(det);
		
		det.calcularImporte();
		fac=facturaDao.save(fac);
		flush();		
		assertNotNull(fac.getId());
		det=fac.getPartidas().iterator().next();
		assertNotNull(det.getId());
		
		e=det.getEntrada();
		assertNotNull(e);
		
		e=entradaPorCompraDao.get(e.getId());
		assertEquals(det.getCosto() ,e.getCosto().setScale(CXPAnalisisDet.SCALE));*/
		
	}
	
	
	/**
	 * Probamos las propiedades en linea analizado y por analizar 
	 * funcionen correctamente
	 * 
	 * 
	 
	public void testPorAnalizdos(){
		CXPFactura fac=new CXPFactura();
		fac.setProveedor(proveedor);
		fac.setDocumento("2535");
		fac.setComentario("CXPFactura de prueba");
		fac.setTotal(BigDecimal.valueOf(23000));
		
		EntradaPorCompra e=entradaPorCompraDao.get(11L);
		
		CXPAnalisisDet det=new CXPAnalisisDet();
		det.setEntrada(e);
		det.setCantidad(e.getPorAnalizar()-1);
		fac.agregarPartida(det);
		fac=facturaDao.save(fac);
		flush();
		
		e=entradaPorCompraDao.get(11L);
		assertEquals(det.getCantidad(), e.getAnalizado());
		assertEquals(1d, e.getPorAnalizar());
		logger.info("Analizado: "+e.getAnalizado());
		logger.info("Por analizar:" +e.getPorAnalizar());
	}*/
	
	
	
	public void setFacturaDao(FacturaDao facturaDao) {
		this.facturaDao = facturaDao;
	}

	
	
	

}
