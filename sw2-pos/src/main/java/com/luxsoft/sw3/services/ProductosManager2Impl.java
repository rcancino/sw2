package com.luxsoft.sw3.services;


import java.util.List;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoRow;
import com.luxsoft.sw3.model.ProductoInfo;
import com.luxsoft.sw3.replica.EntityLog;

@Service("productosManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class ProductosManager2Impl  implements ProductosManager2{
	
	@Autowired
	private ProductoDao productoDao;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;


	public List<Producto> getActivos() {
		return getProductoDao().buscarActivos();
	}
	
	@Override
	public List<ProductoRow> getActivosAsRows() {
		String sql="SELECT X.CLAVE,X.DESCRIPCION,X.GRAMOS, X.ACABADO,X.CARAS,X.NACIONAL,X.ACABADO,X.PRESENTACION,X.KILOS,X.LARGO,X.ANCHO " +
				" ,L.NOMBRE AS LINEA,M.NOMBRE AS MARCA ,C.NOMBRE AS CLASE" +
				" FROM SX_PRODUCTOS X " +
				" LEFT JOIN SX_LINEAS L ON L.LINEA_ID=X.LINEA_ID" +
				" LEFT JOIN SX_MARCAS M ON M.MARCA_ID=X.MARCA_ID" +
				" LEFT JOIN SX_CLASES C  ON C.CLASE_ID=X.CLASE_ID" +
				" WHERE X.ACTIVO IS TRUE ORDER BY X.CLAVE ASC";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper(ProductoRow.class));
	}
	
	
	@Override
	public List<ProductoRow> getActivosDolaresAsRows() {
		String sql="SELECT X.CLAVE,X.DESCRIPCION,X.GRAMOS, X.ACABADO,X.CARAS,X.NACIONAL,X.ACABADO,X.PRESENTACION,X.KILOS,X.LARGO,X.ANCHO " +
				" ,L.NOMBRE AS LINEA,M.NOMBRE AS MARCA ,C.NOMBRE AS CLASE" +
				" FROM SX_PRODUCTOS X " +
				" LEFT JOIN SX_LINEAS L ON L.LINEA_ID=X.LINEA_ID" +
				" LEFT JOIN SX_MARCAS M ON M.MARCA_ID=X.MARCA_ID" +
				" LEFT JOIN SX_CLASES C  ON C.CLASE_ID=X.CLASE_ID" +
				" WHERE X.ACTIVO IS TRUE AND X.precioContadoDolares>0 ORDER BY X.CLAVE ASC";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper(ProductoRow.class));
	}
	
	
	public List<Producto> getProductosParaComprasNacionales(){
		String hql="from Producto p " +
				" where p.activo=true " +
				//" and p.activoCompras=true " +
				" and p.inventariable=true " +
				//" and p.nacional=true " +
				" and p.deLinea=true";
		return hibernateTemplate.find(hql);
	}

	
	public List<Producto> buscarInventariablesActivos() {
		return productoDao.buscarInventariablesActivos();
	}

	public Producto buscarPorClave(final String clave){
		return getProductoDao().buscarPorClave(clave);
	}
	
	public ProductoInfo getProductoInfo(String clave) {
		Producto p=buscarPorClave(clave);		
		if(p!=null){
			ProductoInfo info=new ProductoInfo(p);
			return info;
		}
		return null;
	}

	public boolean exists(Long id) {
		return getProductoDao().exists(id);
	}

	public Producto get(Long id) {
		return getProductoDao().get(id);
	}

	public List<Producto> getAll() {
		return getProductoDao().getAll();
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(Long id) {
		getProductoDao().remove(id);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Producto save(Producto object) {
		Producto res= getProductoDao().save(object);
		
		return res;
	}
	
	
	
	public List<Producto> getMedidasEspeciales() {
		return this.hibernateTemplate.find(
				" from Producto p where " +
				" p.medidaEspecial=true " +
				" and p.activo=true");
	}

	public ProductoDao getProductoDao() {
		return productoDao;
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}
	
	
	

}
