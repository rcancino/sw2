package com.luxsoft.siipap.compras.dao;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.ComprasFactory;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.dao.BaseDaoTestCase;

/**
 * Prueba la persistencia correcta de entradas al inventario
 * por concepto de compra
 * 
 * @author Ruben Cancino
 *
 */
public class EntradaPorCompraDaoTest extends BaseDaoTestCase{
	
	CompraDao compraDao;	
	EntradaPorCompraDao entradaPorCompraDao;
	 
	
	/**
	 * Probamos que una entrada ya existente se pueda leer correctamente
	 * 
	 */
	public void testGetEntrada(){
		
		
	}
	
	
	public void testAddRemove(){
		
		// Leemos una orden de compra existente
		Compra c=compraDao.get(-20L);
		assertFalse(c.getPartidas().isEmpty());
		logger.info("Partidas de la compra:"+c.getPartidas().size());
		
		// Genereamos la entrada a partir de una partida de la compra
		CompraDet det=c.getPartidas().iterator().next();
		EntradaPorCompra e=ComprasFactory.crearEntrada(det);
		e.setCantidad(det.getSolicitado()-1);
		c.actualizar();
		
		// Actualizamos la compra y por presistencia transitoria esperamso que la entrada se persista
		c=compraDao.save(c);
		flush();
		
		// A partir de la compra ya peristida localizamos la entrada
		det=c.getPartidas().iterator().next();
		e=det.getEntradas().iterator().next();
		logger.info("Entradas correctamente persistida: " +e);
		flush();		
		
		//Leemos manualmente la entrada para comprobar su existencia y cantidad
		e=entradaPorCompraDao.get(e.getId());
		//Long eid=e.getId();
		assertEquals(det.getSolicitado()-1, e.getCantidad());		
		
		//Leemos nuevamente la compra desde la base de datos
		c=compraDao.get(c.getId());
		det=c.getPartidas().iterator().next();
		

		// Eliminamos la entrada a partir de la partida de la compra
		boolean res=det.eliminarEntreada(e);
		assertTrue(res);
		c=compraDao.save(c);
		flush();
		
		
		
		try {			
			e=entradaPorCompraDao.get(e.getId());
			logger.info("Entrada localizada:"+e);
			fail("No devio encontrar la entrada");
		} catch (ObjectRetrievalFailureException e2) {
			assertNotNull(e2);
		}
		
	}
	

	public void setCompraDao(CompraDao compraDao) {
		this.compraDao = compraDao;
	}
	public void setEntradaPorCompraDao(EntradaPorCompraDao entradaPorCompraDao) {
		this.entradaPorCompraDao = entradaPorCompraDao;
	}
	
	
	

}
