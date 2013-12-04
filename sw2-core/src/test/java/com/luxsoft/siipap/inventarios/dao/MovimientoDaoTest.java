package com.luxsoft.siipap.inventarios.dao;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.Movimiento.Concepto;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Unidad;

public class MovimientoDaoTest extends BaseDaoTestCase{
	
	private MovimientoDao movimientoDao;
	private Sucursal sucursal;
	private Unidad unidad;

	public void setMovimientoDao(MovimientoDao movimientoDao) {
		this.movimientoDao = movimientoDao;
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		
		List<Sucursal> sucursales=universalDao.getAll(Sucursal.class);
		assertFalse(sucursales.isEmpty());
		sucursal=sucursales.get(0);		
		unidad=(Unidad)universalDao.get(Unidad.class, "MIL");
		assertNotNull(unidad);
	}



	@SuppressWarnings("unchecked")
	public void testAddRemove(){
		//endTransaction();
		Movimiento m=new Movimiento();
		m.setSucursal(sucursal);
		m.setComentario("Movimiento de prueba");
		m.setConcepto(Concepto.CIS);
		List<Producto> prod=universalDao.getAll(Producto.class);
		System.out.println("Productos: "+prod.size());
		for(Producto p:prod){
			MovimientoDet det=new MovimientoDet();
			det.setProducto(p);
			det.setCantidad(10);
			det.setUnidad(unidad);
			m.agregarPartida(det);
		}
		
		m=movimientoDao.save(m);
		flush();
		assertNotNull(m.getId());
		
		m=movimientoDao.get(m.getId());
		for(MovimientoDet det:m.getPartidas()){
			assertNotNull(det.getId());
		}
		
		movimientoDao.remove(m.getId());
		flush();
		try {
			movimientoDao.get(m.getId());
			fail("No debio encontrar el movimiento");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
		
		for(MovimientoDet det:m.getPartidas()){
			try {
				universalDao.get(Inventario.class, det.getId());
				fail("No debio encontrar el inventario");
			} catch (ObjectRetrievalFailureException e) {
				assertNotNull(e);
			}
		}
		
	}
	
	public void testAddBulk(){
		
		
		List<Producto> prod=universalDao.getAll(Producto.class);
		Concepto[] cons=Concepto.values();
		double[] cantidads={10,20,30,40,50,60,12,14,16};
		for(int i=0;i<10;i++){
			Movimiento m=new Movimiento();
			m.setSucursal(sucursal);
			m.setComentario("Movimiento de prueba");
			Concepto c=cons[RandomUtils.nextInt(cons.length)];
			m.setConcepto(c);
			for(int y=0;y<5;y++){
				MovimientoDet det=new MovimientoDet();
				Producto p=prod.get(RandomUtils.nextInt(prod.size()));
				double cantidad=cantidads[RandomUtils.nextInt(cantidads.length)];
				det.setProducto(p);
				det.setCantidad(cantidad);
				det.setUnidad(unidad);
				m.agregarPartida(det);
			}
			m=movimientoDao.save(m);
			assertNotNull(m.getId());
		}		
		setComplete();
		
	}
	

}
