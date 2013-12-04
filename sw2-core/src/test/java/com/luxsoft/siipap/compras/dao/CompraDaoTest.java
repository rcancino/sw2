package com.luxsoft.siipap.compras.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.ComprasFactory;
import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.core.Unidad;

public class CompraDaoTest extends BaseDaoTestCase{
	
	private CompraDao compraDao;
	
	
	public void testGetCompra(){
		Compra c=compraDao.get(-10L);
		assertNotNull(c);
		assertEquals(CantidadMonetaria.pesos(23000), c.getTotal());
	}
	
	
	public void testAddRemoveTest(){
		Compra c=new Compra();
		
		Sucursal s=(Sucursal)universalDao.get(Sucursal.class, 1l);
		assertNotNull(s);
		
		Proveedor p=new Proveedor();
		//p.setNombre("Prov Test_"+System.currentTimeMillis());
		p.setNombre("Prov Test_X");
		p.setClave("PT01");
		c.setProveedor(p);
		c.setSucursal(s);
		agregarPartidas(c);
		c=compraDao.save(c);		
		flush();
		
		assertNotNull(c.getId());
		for(CompraDet det:c.getPartidas()){
			assertNotNull(det.getId());
		}
		
		compraDao.remove(c.getId());
		flush();
		try {
			for(CompraDet det:c.getPartidas()){
				universalDao.get(CompraDet.class,det.getId());
			}
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
			
		}
		try {
			c=compraDao.get(c.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
			
		}
		
	}
	
	private void agregarPartidas(final Compra compra){
		
		Producto p=(Producto)universalDao.get(Producto.class, 500l);		
		CompraDet det=ComprasFactory.crearPartida(compra);
		det.setProducto(p);
		det.setUnidad(p.getUnidad());
		det.setSolicitado(500);
		det.setPrecio(BigDecimal.valueOf(800));
		compra.agregarPartida(det);		 
		
		Producto p2=(Producto)universalDao.get(Producto.class, 501l);
		assertNotNull(p2);
		CompraDet det2=ComprasFactory.crearPartida(compra);		
		det2.setProducto(p);
		det2.setSolicitado(500);
		det2.setPrecio(BigDecimal.valueOf(700));
		det2.setUnidad(p2.getUnidad());
		assertNotNull(p2.getUnidad());
		compra.agregarPartida(det2);
	}
	
	public void testAddBulk(){
		
		List<Sucursal> sucursales=universalDao.getAll(Sucursal.class);
		List<Proveedor> provs=TestUtils.generarProveedoresDePrueba(10);
		Unidad u=(Unidad)universalDao.get(Unidad.class, "MIL");
		assertNotNull(u);
		System.out.println(u);
		List<Producto> prodList=universalDao.getAll(Producto.class);
		
		double[] cants={10,20,30,35,36,45,345,58,59,76,100,250};
		
		BigDecimal[] precios={BigDecimal.valueOf(10),BigDecimal.valueOf(70),BigDecimal.valueOf(10),BigDecimal.valueOf(26),BigDecimal.valueOf(28),BigDecimal.valueOf(30),BigDecimal.valueOf(55)};
		
		for(int i=0;i<90;i++){
			Compra c=new Compra();
			c.setProveedor(provs.get(RandomUtils.nextInt(provs.size())));
			c.setSucursal(sucursales.get(RandomUtils.nextInt(sucursales.size())));
			for(int y=0;y<RandomUtils.nextInt(10);y++){
				CompraDet det=ComprasFactory.crearPartida(c);
				det.setProducto(prodList.get(RandomUtils.nextInt(prodList.size())));
				det.setSolicitado(cants[RandomUtils.nextInt(cants.length)]);
				det.setPrecio(precios[RandomUtils.nextInt(precios.length)]);
				det.setUnidad(u);
				c.agregarPartida(det);
				
			}
			c=compraDao.save(c);
			flush();
		}
		
	}

	public void setCompraDao(CompraDao compraDao) {
		this.compraDao = compraDao;
	}
	
	

}
