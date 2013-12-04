package com.luxsoft.sw3.embarques.dao;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.annotation.NotTransactional;

import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.embarque.EntregaDet;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.embarque.Zona;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

/**
 * Prueba la correcta persistencia de Engregas, totales y parciales
 * 
 * @author Ruben Cancino Ramos
 * 
 */
public class EntregaDaoTest extends VentasBaseDaoTest2 {

	@Autowired
	protected SucursalDao sucursalDao;

	@Autowired
	protected ProductoDao productoDao;

	@Autowired
	protected UniversalDao universalDao;

	@Autowired
	private HibernateTemplate hibernateTemplate;

	private Sucursal sucursal;

	private Transporte transporte;

	private Zona zona;

	@NotTransactional
	@Before
	public void setUp() {
		sucursal = sucursalDao.buscarPorClave(3);
		assertNotNull(sucursal);
		transporte = (Transporte) universalDao.getAll(Transporte.class).get(0);
		assertNotNull(transporte);
		zona = (Zona) universalDao.getAll(Zona.class).get(0);
	}

	@Test
	public void salvarElimiar() {
		Embarque embarque = new Embarque();
		embarque.setFecha(new Date());
		embarque.setComentario("EMBARQUE DE PRUEBA");
		embarque.setSucursal(sucursal.getNombre());
		embarque.setTransporte(transporte);
		//embarque.setSalida(new Date());

		embarque = (Embarque) universalDao.save(embarque);
		agregarEntrega(embarque,true,0);
		flush();

		embarque = (Embarque) universalDao
				.get(Embarque.class, embarque.getId());

		assertEquals("EMBARQUE DE PRUEBA", embarque.getComentario());
		assertNotNull(embarque.getId());

		logger.info("Eliminando pedido: " + embarque.getId());
		universalDao.remove(Embarque.class, embarque.getId());
		flush();

		try {
			embarque = (Embarque) universalDao.get(Embarque.class, embarque
					.getId());
			fail("No debio encotrar el embarque: " + embarque.getId());
		} catch (ObjectRetrievalFailureException ore) {
			logger.debug("Expected exception: " + ore.getMessage());
			assertNotNull(ore);
		}
	}
	
	@Test
	public void salvarElimiarParcial() {
		Embarque embarque = new Embarque();
		embarque.setFecha(new Date());
		embarque.setComentario("EMBARQUE DE PRUEBA");
		embarque.setSucursal(sucursal.getNombre());
		embarque.setTransporte(transporte);
		//embarque.setSalida(new Date());		
		embarque = (Embarque) universalDao.save(embarque);
		agregarEntrega(embarque,true,1);
		flush();

		embarque = (Embarque) universalDao
				.get(Embarque.class, embarque.getId());

		assertEquals("EMBARQUE DE PRUEBA", embarque.getComentario());
		assertNotNull(embarque.getId());

		logger.info("Eliminando pedido: " + embarque.getId());
		universalDao.remove(Embarque.class, embarque.getId());
		flush();

		try {
			embarque = (Embarque) universalDao.get(Embarque.class, embarque
					.getId());
			fail("No debio encotrar el embarque: " + embarque.getId());
		} catch (ObjectRetrievalFailureException ore) {
			logger.debug("Expected exception: " + ore.getMessage());
			assertNotNull(ore);
		}
	}

	private void agregarEntrega(final Embarque embarque,boolean parcial,int index) {
		Entrega entrega = new Entrega();
		
		hibernateTemplate.setMaxResults(5);
		Venta v = (Venta) hibernateTemplate.find("from Venta v left join fetch v.partidas").get(index);
		assertNotNull(v);
		entrega.setComentario("TEST");
		entrega.setFactura(v);
		entrega.setRecibio("TEST");
		//entrega.setZona(zona);
		if(parcial){
			entrega.setParcial(true);
			for(VentaDet det:v.getPartidas()){
				EntregaDet eu=new EntregaDet(det,0.0);
				entrega.getPartidas().add(eu);
			}
		}else
			entrega.setParcial(true);
		entrega.actualziarValor();
		entrega.actualizarKilosCantidad();
		if(v.getPedido()!=null)
			entrega.setInstruccionDeEntrega(v.getPedido().getInstruccionDeEntrega());
		embarque.agregarUnidad(entrega);
	}

	@Test
	@NotTransactional
	public void datosDePrueba() {
		
		sucursal = sucursalDao.buscarPorClave(3);
		for(int index=2; index<=3;index++){
			Embarque embarque = new Embarque();
			embarque.setFecha(new Date());
			embarque.setComentario("EMBARQUE DE PRUEBA");
			embarque.setSucursal(sucursal.getNombre());
			embarque.setTransporte(transporte);
			
			//embarque.setSalida(new Date());
			boolean parcial=index%2==0;
			agregarEntrega(embarque,parcial,index);
			embarque = (Embarque) universalDao.save(embarque);
			//flush();
		}
		
	}

}
