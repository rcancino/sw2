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
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * Implementacion de {@link ExistenciaDao}
 * 
 * @author Ruben Cancino
 *
 */
public class ExistenciaDaoImpl extends GenericDaoHibernate<Existencia, Long> implements ExistenciaDao{

	public ExistenciaDaoImpl() {
		super(Existencia.class);
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
	public Existencia buscar(String producto,long sucursal,int year,int mes){
		final String hql="from Existencia i where i.clave=? and i.sucursal.id=? and i.year=? and i.mes=?";
		List<Existencia> res=getHibernateTemplate().find(hql, new Object[]{producto,sucursal,year,mes});
		return res.isEmpty()?null:res.get(0);
	}
	
	public Existencia buscarEx(String producto,int year,int mes){
		final String hql="from Existencia i where i.clave=?  and i.year=? and i.mes=?";
		List<Existencia> res=getHibernateTemplate().find(hql, new Object[]{producto,year,mes});
		return res.isEmpty()?null:res.get(0);
	}
	
	public Existencia buscarPorClaveSiipap(String producto,int clave,int year,int mes){
		final String hql="from Existencia i where i.clave=? and i.sucursal.clave=? and i.year=? and i.mes=?";
		List<Existencia> res=getHibernateTemplate().find(hql, new Object[]{producto,clave,year,mes});
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
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoDeExistencia.sql");
				
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
			Number sucursalId=(Number)row.get("SUCURSAL_ID");
			String clave=(String)row.get("CLAVE");
			Existencia target=buscar(clave, sucursalId.longValue(),year,mes);
			if(target==null){				
				target=new Existencia();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes);
				target.setYear(year);
				Producto p=getProductoDao().buscarPorClave(clave);
				Sucursal s=buscarSucursal(sucursalId.longValue());
				target.setProducto(p);
				target.setSucursal(s);
			}
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
			Number sucursalId=(Number)row.get("SUCURSAL_ID");
			Existencia target=buscar(clave, sucursalId.longValue(), year,mes);
			if(target==null){				
				target=new Existencia();
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
	
	public void actualizarExistencias(Long sucursalId,int year,int mes){
						
		Assert.isTrue( (mes>=1 && mes<=12) ,"El rango del mes debe ser 1 - 12");		
		final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);		
		/*		
		String sql="select clave from SX_EXISTENCIAS x  " +
				" where  x.sucursal_id=? and fecha<=?  " +
				" group by clave " +
				" order by clave";
		SqlParameterValue p1=new SqlParameterValue(Types.NUMERIC,sucursalId);
		SqlParameterValue p2=new SqlParameterValue(Types.DATE,per.getFechaFinal());
		final List<String> claves=getJdbcTemplate().queryForList(sql, new Object[]{p1,p2},String.class);
		logger.info("Claves a procesar :"+claves.size());
		for(String clave:claves){
			try {
				actualizarExistencias(sucursalId, clave, year, mes);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getRootCauseMessage(e));
			}
		}
		*/
		
		logger.info("Actualizando existencias para la sucursal: "+sucursalId+" Periodo: "+mes+"/"+year );
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoDeExistenciaPorSucursal.sql");
		sql=sql.replaceAll("@SUCURSAL", sucursalId.toString());		
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
			Existencia target=buscar(clave, sucursalId, year,mes);
			
			String slq2="select IFNULL(SUM(ifnull(((X.SOLICITADO-X.DEPURADO))-IFNULL((SELECT SUM(I.CANTIDAD) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID),0),0)),0) AS PENDTE"+
		   			" from sx_compras2 C JOIN sx_compras2_det X  ON(C.COMPRA_ID=X.COMPRA_ID) WHERE X.CLAVE=? AND X.SUCURSAL_ID=?"+
		   			" AND ((X.SOLICITADO-X.DEPURADO))-IFNULL((SELECT SUM(I.CANTIDAD) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID),0)>0";

		   	Double pendiente=(Double) getJdbcTemplate().queryForObject(slq2, new Object[]{clave,sucursalId}, Double.class);	
			
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
			Existencia targetAnt=buscar(clave, sucursalId, year,mesAnt);
						
			
			if(target==null){				
				target=new Existencia();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes);
				target.setYear(year);
				Producto p=getProductoDao().buscarPorClave(clave);
				Sucursal s=buscarSucursal(sucursalId);
				target.setProducto(p);
				target.setSucursal(s);
				if (targetAnt!=null){
					target.setRecorte(targetAnt.getRecorte());
					target.setRecorteComentario(targetAnt.getRecorteComentario());
					target.setRecorteFecha(targetAnt.getRecorteFecha());	
				}
					
			}
			target.setPedidosPendientes(pendiente);					
			target.setCantidad(cantidad.doubleValue());
			target.setModificado(new Date());
			
			try {
				target=save(target);
			} catch (Exception e) {
				System.out.println("No se pudo actualizar la existencia de: "+row);
			}
						
		}
	}
	
	public void actualizarExistencias(Long sucursalId,String clave,int year,int mes){
		logger.info("Actualizando existencias para: "+clave+" "+mes+"/"+year + "  Sucursal: "+sucursalId);
		System.out.println("Actualizando existencias para: "+clave+" "+mes+"/"+year + "  Sucursal: "+sucursalId);
		Assert.isTrue( (mes>=1 && mes<=12) ,"El rango del mes debe ser 1 - 12");
		final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);		
				
		/*String sql="select sucursal_id,sum(x.cantidad) as CANTIDAD, sum(x.kilos) as KILOS from v_inv x  " +
				" where x.clave=? " +
				" and x.sucursal_id=? " +
				" and fecha<=?   " +
				" group by sucursal_id "
				;*/
		String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoDeExistenciaPorClave.sql");
		
		sql=sql.replaceAll("@CLAVE", clave);
		sql=sql.replaceAll("@SUCURSAL", sucursalId.toString());		
		
		sql=sql.replaceAll("@FECHA_INI", "2009/01/01");
		SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
	    String fecha1=  formato.format(per.getFechaFinal());
	   	sql=sql.replaceAll("@CORTE_FIN",fecha1 );
	   	sql=sql.replaceAll("@CORTE","2009/01/01" );
	   	
		String slq2="select IFNULL(SUM(ifnull(((X.SOLICITADO-X.DEPURADO))-IFNULL((SELECT SUM(I.CANTIDAD) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID),0),0)),0) AS PENDTE"+
	   			" from sx_compras2 C JOIN sx_compras2_det X  ON(C.COMPRA_ID=X.COMPRA_ID) WHERE X.CLAVE=? AND X.SUCURSAL_ID=?"+
	   			" AND ((X.SOLICITADO-X.DEPURADO))-IFNULL((SELECT SUM(I.CANTIDAD) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID),0)>0";

	   	Double pendiente=(Double) getJdbcTemplate().queryForObject(slq2, new Object[]{clave,sucursalId}, Double.class);
	   	
		//System.out.println(sql);
		final List<Map<String, Object>> rows=getJdbcTemplate().queryForList(sql);		
		for(Map<String,Object> row:rows){			
			Number cantidad=(Number)row.get("CANTIDAD");
			Existencia target=buscar(clave, sucursalId, year,mes);
			if(target==null){				
				target=new Existencia();
				target.setFecha(per.getFechaFinal());
				target.setMes(mes);
				target.setYear(year);
				Producto p=getProductoDao().buscarPorClave(clave);
				Sucursal s=buscarSucursal(sucursalId);
				target.setProducto(p);
				target.setSucursal(s);
			}
			target.setPedidosPendientes(pendiente);
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
	public double calcularExistencia(final Producto producto,final Sucursal sucursal,final Date fecha){
		List<Number> res=getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery("select sum(i.cantidad) from Inventario i where i.producto=? amd i.sucursal.id=? and i.fecha<=?")
				.setEntity(0, producto)
				.setEntity(1,sucursal)
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
	public List<Existencia> buscarExistencias(Producto producto, Date fecha) {
		final int year=Periodo.obtenerYear(fecha);
		final int mes=Periodo.obtenerMes(fecha)+1;
		final String hql="from Existencia i where i.clave=? and i.year=? and i.mes=?";
		logger.info("Buscando existencias para :"+fecha);
		return getHibernateTemplate().find(hql, new Object[]{producto.getClave(),year,mes});
	}


	
	public List<Existencia> buscarExistencias(Long sucursalId, Date fecha) {
		final int year=Periodo.obtenerYear(fecha);
		final int mes=Periodo.obtenerMes(fecha)+1;
		final String hql="from Existencia i where i.sucursal.id=? and i.year=? and i.mes=?";
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
	public Existencia generar(final Producto producto,final Date fecha, final Long sucursalId){
		
		int year=Periodo.obtenerYear(fecha);
		int mes=Periodo.obtenerMes(fecha)+1;
		
		Existencia e=buscar(producto.getClave(), sucursalId, year, mes);
		
		if(e==null){
			e=new Existencia();
			e.setSucursal(buscarSucursal(sucursalId));
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
	public Existencia generar(final String  clave,final Date fecha, final Long sucursalId){	
		int year=Periodo.obtenerYear(fecha);
		int mes=Periodo.obtenerMes(fecha)+1;		
		Existencia e=buscar(clave, sucursalId, year, mes);		
		if(e==null){
			e=new Existencia();
			e.setSucursal(buscarSucursal(sucursalId));
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
	public Existencia generar(String clave, Long sucursalId, int year, int mes) {
		Existencia e=buscar(clave, sucursalId, year, mes);
		Calendar c=Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, mes-1);
		Date fecha=c.getTime();
		if(e==null){
			e=new Existencia();
			e.setSucursal(buscarSucursal(sucursalId));
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


	public Sucursal buscarSucursal(final Long clave){
		return (Sucursal)getHibernateTemplate().get(Sucursal.class, clave);
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		//Set<Long>
		ServiceLocator2
		.getExistenciaDao()
		//.generar("LEDB100L", 3L, 2010,8)
		//.actualizarExistencias(3L,2010, 11)
		.actualizarExistencias("BB66.7", 2013, 3);
	}
	
	
}
