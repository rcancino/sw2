package com.luxsoft.siipap.inventarios.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.dao.core.ProductoDao;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.inventarios.model.ExistenciaMaq;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.maquila.model.Almacen;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * Implementacion de {@link ExistenciaMaqDao}
 * 
 * @author Ruben Cancino
 *
 */
public class ExistenciaMaqDaoImpl extends GenericDaoHibernate<ExistenciaMaq, Long> implements ExistenciaMaqDao{

	public ExistenciaMaqDaoImpl() {
		super(ExistenciaMaq.class);
	}
	
	/**
	 * Localiza el inventario inicial para un producto, sucursal  año y mes 
	 * 
	 * @param producto La clave del producto
	 * @param sucursal El Id de la sucursal
	 * @param year El año
	 * @param mes  El mes (valido 1 al 12)
	 * @return
	 */
	@Transactional(propagation=Propagation.SUPPORTS)
	public ExistenciaMaq buscar(String producto,long almacen,int year,int mes){
		final String hql="from ExistenciaMaq i where i.clave=? and i.almacen.id=? and i.year=? and i.mes=?";
		List<ExistenciaMaq> res=getHibernateTemplate().find(hql, new Object[]{producto,almacen,year,mes});
		return res.isEmpty()?null:res.get(0);
	}
	
	public ExistenciaMaq buscarEx(String producto,int year,int mes){
		final String hql="from ExistenciaMaq i where i.clave=?  and i.year=? and i.mes=?";
		List<ExistenciaMaq> res=getHibernateTemplate().find(hql, new Object[]{producto,year,mes});
		return res.isEmpty()?null:res.get(0);
	}
	
	public ExistenciaMaq buscarPorClaveSiipap(String producto,int clave,int year,int mes){
		final String hql="from ExistenciaMaq i where i.clave=? and i.almacen.clave=? and i.year=? and i.mes=?";
		List<ExistenciaMaq> res=getHibernateTemplate().find(hql, new Object[]{producto,clave,year,mes});
		return res.isEmpty()?null:res.get(0);
	}
	
	/**
	 * Actualiza las existencias de todas las sucursales y para todos los articulos al final del mes
	 * 
	 * @param year
	 * @param mes
	 */
	public void actualizarExistencias(int year, int mes){
		Assert.isTrue( (mes>=1 && mes<=12) ,"El rango del mes debe ser 1 - 12");		
		final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);
		logger.info("Actualizando existencias  Periodo: "+mes+"/"+year );
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoExistenciaMaquila.sql");
				
		/*sql=sql.replaceAll("@FECHA_INI", "2009/01/01");
		SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
	    String fecha1=  formato.format(per.getFechaFinal());
	   	sql=sql.replaceAll("@CORTE_FIN",fecha1 );
	   	sql=sql.replaceAll("@CORTE","2009/01/01" );*/
	   	
		System.out.println(sql);
		final List<Map<String, Object>> rows=getJdbcTemplate().queryForList(sql);
		System.out.println("CLAVES: "+rows.size());
		for(Map<String,Object> row:rows){			
			Number cantidad=(Number)row.get("CANTIDAD");
			Number almacenId=(Number)row.get("ALMACEN_ID");
			String clave=(String)row.get("CLAVE");
			
			
			ExistenciaMaq target=buscar(clave, almacenId.longValue(),year,mes);
			if(target==null){	
				System.out.println("Clave  "+ clave);
				System.out.println("Esta existencia de Maq no existe se procede a crearla");
				
				target=new ExistenciaMaq();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes);
				target.setYear(year);
				Producto p=getProductoDao().buscarPorClave(clave);
				Almacen a=buscarAlmacen(almacenId.longValue());
				target.setProducto(p);
				target.setAlmacen(a);
			}else {
				System.out.println("-----"+target.getClave());
			//target.setCantidad(cantidad.doubleValue());			
			}
			try {
			 //	target=save(target);
			} catch (Exception e) {
				System.out.println("No se pudo actualizar la existencia de: "+row);
			}
						
		}
	}
	


	
	
	public void actualizarExistencias(){
	//	Assert.isTrue( (mes>=1 && mes<=12) ,"El rango del mes debe ser 1 - 12");		
	//	final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);
	//	logger.info("Actualizando existencias  Periodo: "+mes+"/"+year );
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoExistenciaMaquila.sql");
				
		/*sql=sql.replaceAll("@FECHA_INI", "2009/01/01");
		SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
	    String fecha1=  formato.format(per.getFechaFinal());
	   	sql=sql.replaceAll("@CORTE_FIN",fecha1 );
	   	sql=sql.replaceAll("@CORTE","2009/01/01" );*/
	   	
		//System.out.println(sql);
		final List<Map<String, Object>> rows=getJdbcTemplate().queryForList(sql);
		System.out.println("Actualizando Existencia de Maquila   Total claves por actualizar: "+rows.size());
		for(Map<String,Object> row:rows){			
			Number cantidad=(Number)row.get("CANTIDAD");
			Number almacenId=(Number)row.get("ALMACEN_ID");
			String clave=(String)row.get("CLAVE");
			Long mes=(Long) row.get("MES");
			Long year=(Long) row.get("YEAR");
			
			final Periodo per=Periodo.getPeriodoEnUnMes(mes.intValue(), year.intValue());
		
		
			ExistenciaMaq target=buscar(clave, almacenId.longValue(),year.intValue(),mes.intValue());
			if(target==null){	
				System.out.println("Clave  "+ clave);
				System.out.println("Esta existencia de Maq no existe se procede a crearla");
				
				target=new ExistenciaMaq();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes.intValue());
				target.setYear(year.intValue());
				Producto p=getProductoDao().buscarPorClave(clave);
				Almacen a=buscarAlmacen(almacenId.longValue());
				target.setProducto(p);
				target.setAlmacen(a);
			}
			//	System.out.println("-----"+target.getClave());
			target.setCantidad(cantidad.doubleValue());			
			
			try {
			 target=save(target);
			} catch (Exception e) {
				System.out.println("No se pudo actualizar la existencia de: "+row);
			}
						
		}
	}
	
	
	
	/**
	 * Actualiza las existencias para el articulo indicado en el año y  al fin del mes solicitado
	 * 
	 * @param clave La clave del articulo
	 * @param year  El año o periodo fiscal
	 * @param mes   El mes (Puede ser entre 2= Febrero hasta 12 Diciembre
	 * @return La existencia ya persistida
	 */
	public void actualizarExistencias(String clave,int year, int mes){
		logger.info("Actualizando existencias para: "+clave+" "+mes+"/"+year);
		Assert.isTrue( (mes>=1 && mes<=12) ,"El rango del mes debe ser 1 - 12");
		final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoDeExistenciaPorClaveTodasSucursales.sql");
		DateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		sql=sql.replaceAll("@CLAVE", clave);
		System.out.println(clave);
		
		Calendar cal=Calendar.getInstance();
		cal.setTime(per.getFechaInicial());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, mes-1);
		cal.getTime();
		
		cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		String corteFin=df.format(cal.getTime());
		sql=sql.replaceAll("@CORTE_FIN", corteFin);	
		sql=sql.replaceAll("@CORTE","2009/01/01" );
		
		
		sql=sql.replaceAll("@FECHA_INI", "2009/01/01");
		System.out.println(sql);
		final List<Map<String, Object>> rows=getJdbcTemplate().queryForList(sql);		
		for(Map<String,Object> row:rows){			
			Number cantidad=(Number)row.get("CANTIDAD");
			Number almacenId=(Number)row.get("SUCURSAL_ID");
			ExistenciaMaq target=buscar(clave, almacenId.longValue(), year,mes);
			if(target==null){				
				target=new ExistenciaMaq();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes);
				target.setYear(year);
				Producto p=getProductoDao().buscarPorClave(clave);
				target.setProducto(p);
			
			}
			target.setCantidad(cantidad.doubleValue());	
			try {
				target=save(target);
			} catch (Exception e) {
				System.out.println("No se pudo actualizar la existencia de: "+row);
			}
						
		}
		
	}
	
	public void actualizarExistencias(Long almacenId,int year,int mes){
						
		Assert.isTrue( (mes>=1 && mes<=12) ,"El rango del mes debe ser 1 - 12");		
		final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);		

		
		logger.info("Actualizando existencias para la sucursal: "+almacenId+" Periodo: "+mes+"/"+year );
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoDeExistenciaPorSucursal.sql");
		sql=sql.replaceAll("@SUCURSAL", almacenId.toString());		
		sql=sql.replaceAll("@FECHA_INI", "2009/01/01");
		SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
	    String fecha1=  formato.format(per.getFechaFinal());
	   	sql=sql.replaceAll("@CORTE_FIN",fecha1 );
	   	sql=sql.replaceAll("@CORTE","2009/01/01" );
	   	
		System.out.println(sql);
		final List<Map<String, Object>> rows=getJdbcTemplate().queryForList(sql);
		System.out.println("CLAVES: "+rows.size());
		for(Map<String,Object> row:rows){			
			Number cantidad=(Number)row.get("CANTIDAD");
			String clave=(String)row.get("CLAVE");
			ExistenciaMaq target=buscar(clave, almacenId, year,mes);
			
		
			int mesAnt,yearAnt;
			
			if(mes-1==0)
			{
				 mesAnt=12;
				 yearAnt=year-1;
			}
			else{
				mesAnt=mes-1;
				yearAnt=year;
			}
			ExistenciaMaq targetAnt=buscar(clave, almacenId, year,mesAnt);
						
			
			if(target==null){				
				target=new ExistenciaMaq();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes);
				target.setYear(year);
				Producto p=getProductoDao().buscarPorClave(clave);
				//Sucursal s=buscarSucursal(sucursalId);
				target.setProducto(p);
				//target.setSucursal(s);
				
					
			}
							
			target.setCantidad(cantidad.doubleValue());
			target.setModificado(new Date());
			
			try {
				target=save(target);
			} catch (Exception e) {
				System.out.println("No se pudo actualizar la existencia de: "+row);
			}
						
		}
	}
	
	public void actualizarExistencias(Long almacenId,String clave,int year,int mes){
		logger.info("Actualizando existencias para: "+clave+" "+mes+"/"+year + "  Sucursal: "+almacenId);
		System.out.println("Actualizando existencias para: "+clave+" "+mes+"/"+year + "  Sucursal: "+almacenId);
		Assert.isTrue( (mes>=1 && mes<=12) ,"El rango del mes debe ser 1 - 12");
		final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);		
	
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoDeExistenciaPorClave.sql");
		
		sql=sql.replaceAll("@CLAVE", clave);
		sql=sql.replaceAll("@SUCURSAL", almacenId.toString());		
		
		sql=sql.replaceAll("@FECHA_INI", "2009/01/01");
		SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
	    String fecha1=  formato.format(per.getFechaFinal());
	   	sql=sql.replaceAll("@CORTE_FIN",fecha1 );
	   	sql=sql.replaceAll("@CORTE","2009/01/01" );
	   	
	
		final List<Map<String, Object>> rows=getJdbcTemplate().queryForList(sql);		
		for(Map<String,Object> row:rows){			
			Number cantidad=(Number)row.get("CANTIDAD");
			ExistenciaMaq target=buscar(clave, almacenId, year,mes);
			if(target==null){				
				target=new ExistenciaMaq();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes);
				target.setYear(year);
				Producto p=getProductoDao().buscarPorClave(clave);
				Almacen s=buscarAlmacen(almacenId);
				target.setProducto(p);
				target.setAlmacen(s);
			}
			
			target.setCantidad(cantidad.doubleValue());
			target.setModificado(new Date());
			
			try {
				target=save(target);
			} catch (Exception e) {
				System.out.println("No se pudo actualizar la existencia de: "+row);
			}
						
		}
	}
	
	/**
	 * Calcula y regresa la existencia 
	 * 
	 * @param producto El producto
	 * @param sucursal La sucursal 
	 * @param fecha    A la fecha indicada (inclusiva)
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public double calcularExistencia(final Producto producto,final Almacen almacen,final Date fecha){
		List<Number> res=getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery("select sum(i.cantidad) from Inventario i where i.producto=? and i.sucursal.id=? and i.fecha<=?")
				.setEntity(0, producto)
				.setEntity(1,almacen)
				.setParameter(2, fecha,Hibernate.DATE)
				.list();
			}
			
		});
		return res.isEmpty()?0.0d:res.get(0).doubleValue();
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.inventarios.dao.ExistenciaDao#buscarExistencias(com.luxsoft.siipap.model.core.Producto, java.util.Date)
	 */
	public List<ExistenciaMaq> buscarExistencias(Producto producto, Date fecha) {
		final int year=Periodo.obtenerYear(fecha);
		final int mes=Periodo.obtenerMes(fecha)+1;
		final String hql="from ExistenciaMaq i where i.clave=? and i.year=? and i.mes=?";
		logger.info("Buscando existencias para :"+fecha);
		return getHibernateTemplate().find(hql, new Object[]{producto.getClave(),year,mes});
	}


	
	public List<ExistenciaMaq> buscarExistencias(Long sucursalId, Date fecha) {
		final int year=Periodo.obtenerYear(fecha);
		final int mes=Periodo.obtenerMes(fecha)+1;
		final String hql="from ExistenciaMaq i where i.sucursal.id=? and i.year=? and i.mes=?";
		logger.info("Buscando existencias para :"+fecha);
		return getHibernateTemplate().find(hql, new Object[]{sucursalId,year,mes});
	}

	
	
	/**
	 * Genera un registro de existencia
	 * 
	 * Si el registro ya existe lo regresa intacto
	 * 
	 * @param producto
	 * @param fecha
	 * @return
	 */
	public ExistenciaMaq generar(final Producto producto,final Date fecha, final Long sucursalId){
		
		int year=Periodo.obtenerYear(fecha);
		int mes=Periodo.obtenerMes(fecha)+1;
		
		ExistenciaMaq e=buscar(producto.getClave(), sucursalId, year, mes);
		
		if(e==null){
			e=new ExistenciaMaq();
			//e.setSucursal(buscarSucursal(sucursalId));
			e.setProducto(producto);
			e.setCreateUser("ADMIN");
			e.setFecha(fecha);
			e.setYear(year);
			e.setMes(mes);
			e=save(e);
		}
		return e;
	}
	
	/**
	 * Genera un registro de existencia si este ya existe lo regresa
	 * 
	 * @param clave
	 * @param fecha
	 * @param sucursalId
	 * @return
	 */
	public ExistenciaMaq generar(final String  clave,final Date fecha, final Long almacenId){	
		int year=Periodo.obtenerYear(fecha);
		int mes=Periodo.obtenerMes(fecha)+1;		
		ExistenciaMaq e=buscar(clave, almacenId, year, mes);		
		if(e==null){
			e=new ExistenciaMaq();
			e.setAlmacen(buscarAlmacen(almacenId));
			Producto producto=getProductoDao().buscarPorClave(clave);
			e.setProducto(producto);
			e.setCreateUser("ADMIN");
			e.setFecha(fecha);
			e.setYear(year);
			e.setMes(mes);
			e=save(e);
		}
		return e;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.inventarios.dao.ExistenciaDao#generar(java.lang.String, java.lang.Long, int, int)
	 */
	public ExistenciaMaq generar(String clave, Long almacenId, int year, int mes) {
		ExistenciaMaq e=buscar(clave, almacenId, year, mes);
		Calendar c=Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, mes-1);
		Date fecha=c.getTime();
		if(e==null){
			e=new ExistenciaMaq();
			e.setAlmacen(buscarAlmacen(almacenId));
			Producto producto=getProductoDao().buscarPorClave(clave);
			e.setProducto(producto);
			e.setCreateUser("ADMIN");
			e.setFecha(fecha);
			e.setYear(year);
			e.setMes(mes);
			e=save(e);
		}
		return e;
	}



	private ProductoDao productoDao;
	
	public ProductoDao getProductoDao() {
		return productoDao;
	}

	public void setProductoDao(ProductoDao productoDao) {
		this.productoDao = productoDao;
	}
	
	private JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

/* Revisar para ajustar a almacen y se pueda buscar el Almacen*/
	public Almacen buscarAlmacen(final Long id){
		return (Almacen)getHibernateTemplate().get(Almacen.class, id);
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		//Set<Long>
		ServiceLocator2
		//.getExistenciaMaqDao()
		//.generar("LEDB100L", 3L, 2010,8)
		//.actualizarExistencias(3L,2010, 11)
		//.actualizarExistencias("BB66.7", 2013, 3);
		.getExistenciaMaqDao().actualizarExistencias();
	}
	
	
}
