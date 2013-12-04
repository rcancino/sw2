package com.luxsoft.sw3.dao;

import java.util.Date;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

public class TransformacionDaoTest extends VentasBaseDaoTest2{
	
	@Autowired
	ProductoDao productoDao;
	@Autowired
	protected SucursalDao sucursalDao;
	@Autowired
	protected UniversalDao universalDao;
	
	private Producto origen;
	private Producto destino;
	private Sucursal sucursal;
	
	
	@Before
	public void setUp(){
		
		origen=productoDao.buscarPorClave("POL74");
		destino=productoDao.buscarPorClave("POL74.5");
		sucursal=sucursalDao.buscarPorClave(1);
		
		assertNotNull(origen);
		assertNotNull(destino);
		assertNotNull(sucursal);
	}

	@NotTransactional
	@Test
	public void AddRemove(){
		Transformacion t=new Transformacion();
		t.setComentario("TEST");
		t.setFecha(new Date());
		t.setPorInventario(Boolean.FALSE);
		t.setSucursal(sucursal);
		
		TransformacionDet salida=getDet(origen, -50);
		salida.setConceptoOrigen("TRS");
		TransformacionDet entrada=getDet(destino, 50);
		entrada.setConceptoOrigen("TRS");
		
		salida.setDestino(entrada);
		
		t.agregarTransformacion(salida);
		t.agregarTransformacion(entrada);
		
		t=(Transformacion)universalDao.save(t);
		assertNotNull(t.getId());
		flush();
		
		t=(Transformacion)universalDao.get(Transformacion.class, t.getId());
		assertNotNull(t);
		assertEquals("TEST",t.getComentario() );
		
		
		
	}
	
	private TransformacionDet getDet(Producto p,double cantidad){
		TransformacionDet det=new TransformacionDet();
		det.setProducto(p);
		det.setCantidad(cantidad);
		det.setSucursal(sucursal);
		return det;
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}

	public void setSucursalDao(SucursalDao sucursalDao) {
		this.sucursalDao = sucursalDao;
	}
	
	

}
