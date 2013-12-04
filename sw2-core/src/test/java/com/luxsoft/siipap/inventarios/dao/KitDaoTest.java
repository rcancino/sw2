package com.luxsoft.siipap.inventarios.dao;

import java.math.BigDecimal;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.KitDet;
import com.luxsoft.siipap.model.Sucursal;

/**
 * Prueba la implementacion de KitDao para la persistencia
 * de entidades de Kit/KitDet 
 * 
 * @author RUBEN
 *
 */
public class KitDaoTest extends BaseDaoTestCase{
	
	private KitDao kitDao;
	private SucursalDao sucursalDao;
	private ProductoDao productoDao;
	private Sucursal sucursal;
	
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		sucursal=sucursalDao.buscarPorClave(1);
	}
	
	

	public void testAddRemove(){
		Kit kit=new Kit();
		kit.setSucursal(sucursal);
		kit.setComentario("Prueba test de kit");
		
		KitDet target=new KitDet();
		target.setSucursal(sucursal);
		target.setCantidad(400);
		target.setProducto(productoDao.get(new Long(500)));
		target.setCosto(BigDecimal.valueOf(40));
		target.setCostoPromedio(target.getCosto());		
		kit.setEntrada(target);
		
		KitDet sal1=new KitDet();
		sal1.setSucursal(sucursal);
		sal1.setCantidad(400);
		sal1.setProducto(productoDao.get(new Long(500)));
		sal1.setCosto(BigDecimal.valueOf(40));
		sal1.setCostoPromedio(target.getCosto());
		kit.agregarSalida(sal1);
		
		kit=kitDao.save(kit);
		flush();		
		assertNotNull(kit.getId());
		assertNotNull(kit.getEntrada().getId());
		assertNotNull(kit.getSalidas().iterator().next().getId());
		
	}

	public void setKitDao(KitDao kitDao) {
		this.kitDao = kitDao;
	}
	public void setSucursalDao(SucursalDao sucursalDao) {
		this.sucursalDao = sucursalDao;
	}
	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}
	
	

}
