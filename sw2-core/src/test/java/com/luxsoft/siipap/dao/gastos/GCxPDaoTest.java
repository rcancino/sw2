package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.gastos.GCXPFactory;
import com.luxsoft.siipap.model.gastos.GCXPFactoryImpl;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCxP;
import com.luxsoft.siipap.model.gastos.GProveedor;

@Deprecated
public class GCxPDaoTest extends BaseDaoTestCase{
	
	private GCxPDao cxpDao;
	private GCompraDao compraDao;
	private GProveedorDao proveedorDao;
	
	public static final String PROVEEDOR="PROV01";
	
	private GCXPFactory factory=new GCXPFactoryImpl();
	
	
	
	public void testFindByProveedor(){
		final GProveedor p=proveedorDao.buscarPorNombre(PROVEEDOR);
		assertNotNull(p);
		List<GCxP> l=cxpDao.buscarPorProveedor(p);
		assertFalse("Debe existir por lo menos un cxp para el proveedor"+PROVEEDOR,l.isEmpty());		
	}
	
	public void testAddRemove(){
		final GProveedor p=proveedorDao.buscarPorNombre(PROVEEDOR);
		assertNotNull("Debe existir el proveedor "+PROVEEDOR,p);
		final GCompra compra=compraDao.buscarPorProveedor(p).get(0);
		assertNotNull("Debe existir por lo menos una compra para el proveedor: "+p.getNombre(),compra);
		
		GCxP cxp=factory.createCxP(compra);
		assertNotNull(cxp);
		cxp=cxpDao.save(cxp);
		assertNotNull(cxp.getId());
		flush();
		setComplete();
		
		
	}

	public void setCxpDao(GCxPDao cxpDao) {
		this.cxpDao = cxpDao;
	}
	public void setCompraDao(GCompraDao compraDao) {
		this.compraDao = compraDao;
	}
	public void setProveedorDao(GProveedorDao proveedorDao) {
		this.proveedorDao = proveedorDao;
	}
	
	
	

}
