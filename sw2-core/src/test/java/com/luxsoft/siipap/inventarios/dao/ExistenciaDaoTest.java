package com.luxsoft.siipap.inventarios.dao;


import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;

/**
 *
 *  
 * @author Ruben Cancino
 *
 */
public class ExistenciaDaoTest extends BaseDaoTestCase{
	
	private ExistenciaDao existenciaDao;
	private ProductoDao productoDao;
	private SucursalDao sucursalDao;
	
	/*
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		deleteFromTables(new String[]{"SX_INVENTARIO_COM"});
		insertDataSet("dbunit/entradas_com.xml");
	}	
*/
	public void testAdd(){
		
	}
	

	public void setExistenciaDao(ExistenciaDao existenciaDao) {
		this.existenciaDao = existenciaDao;
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}

	public void setSucursalDao(SucursalDao sucursalDao) {
		this.sucursalDao = sucursalDao;
	}
	
	

}
