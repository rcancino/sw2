package com.luxsoft.siipap.inventarios.service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.inventarios.dao.CostoPromedioDao;
import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.inventarios.model.CostoPromedioItem;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.core.ProductoManager;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * Implementacion de CostoPromedioManager para calcular los costos promedios de los articulos
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CostoPromedioManagerImpl extends GenericManagerImpl<CostoPromedio,Long> implements CostoPromedioManager{
	
	private Logger logger=Logger.getLogger(getClass());
	
	private HibernateTemplate hibernateTemplate;
	
	private JdbcTemplate jdbcTemplate;	
	
	private ProductoManager productoManager;

	public CostoPromedioManagerImpl(CostoPromedioDao genericDao) {
		super(genericDao);
	}
	
	public CostoPromedioDao getDao(){
		return (CostoPromedioDao)this.genericDao;
	}
	
	public CostoPromedio buscarCostoPromedio(final int year,final int mes,String clave){		
		return getDao().buscar(clave, year, mes);
	}
	
	
	
	/**
	 * Actualiza el costo promedio para un articulo en particular
	 * 
	 * @param year
	 * @param mes
	 * @param clave
	 */
	public void actualizarCostoPromedio(final int year,final int mes,String clave){
		logger.info("Actualizando Costo Promedio para: "+clave+" "+mes+"/"+year);
		CostoPromedio cp=buscarCostoPromedio(year, mes, clave);
		if(cp==null){
			Producto prod=getProductoManager().buscarPorClave(clave);
			Assert.notNull(prod,"No existe el producto: "+clave);
			cp=new CostoPromedio(year,mes,prod);
		}		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/costo_promedio.sql");
		sql=sql.replaceAll("@CLAVE", "\'"+clave+"\'");
		sql=sql.replaceAll("@YEAR", String.valueOf(year));
		sql=sql.replaceAll("@MONTH", String.valueOf(mes));
		
		int mes_ini=mes;
		int year_ini=year;
		if(mes==1){
			mes_ini=12;
			year_ini=year-1;
		}else{
			mes_ini=mes-1;
		}
		//System.out.println(sql);
		//System.out.println("Año para exis: "+year_ini+"  Mes exis: "+mes_ini);
		List<Map<String,Object>> rows=getJdbcTemplate()
			.queryForList(sql,new Object[]{year_ini,mes_ini});
		
		BigDecimal cantidadTotal=BigDecimal.ZERO;
		CantidadMonetaria importeTotal=CantidadMonetaria.pesos(0);
		
		BigDecimal cantidadInicial=BigDecimal.ZERO;
		CantidadMonetaria importeInicial=CantidadMonetaria.pesos(0);
		
		for(Map<String,Object> row:rows){
			
			String origen=(String)row.get("ORIGEN");
			BigDecimal cantidadRow=new BigDecimal( ((Number)row.get("CANTIDAD")).doubleValue()).setScale(3,RoundingMode.HALF_EVEN);
			Number costo=(Number)row.get("COSTO");
			
			CantidadMonetaria importe=CantidadMonetaria.pesos(costo.doubleValue());
			importe=importe.multiply(cantidadRow.doubleValue());
			
			if("INI".equals(origen)){
				cantidadInicial=cantidadInicial.add(cantidadRow);
				importeInicial=importeInicial.add(importe);
				
			}else{
				cantidadTotal=cantidadTotal.add(cantidadRow);
				importeTotal=importeTotal.add(importe);
			}
			
		}
		
		CantidadMonetaria costop=CantidadMonetaria.pesos(0);
		if(cantidadInicial.doubleValue()>0){
			cantidadTotal=cantidadTotal.add(cantidadInicial);
			importeTotal=importeTotal.add(importeInicial);
		}
		
		if(cantidadTotal.doubleValue()!=0){			
			costop=importeTotal.divide(cantidadTotal);
		}
		cp.setCostop(costop.amount());
		
		cp=save(cp);
		System.out.println("Costo registrado: "+cp);
		List<Existencia> exis=getHibernateTemplate()
		.find("from Existencia e where e.clave=? and e.year=? and e.mes=?",new Object[]{clave,year,mes}); 
		for(Existencia e:exis){
			e.setCosto(costop.amount());
			e.setCostoPromedio(costop.amount());
			getHibernateTemplate().saveOrUpdate(e);
		}
		forwardCosto(year,clave);
		backwardCosto(year,clave);
	}
	
	
	

	
	/**
	 * Verifica que una vez que exita costo promedio en un articulo este se
	 * traslade a meses <b>POSTERIORES</b> que no tengan costo</p>
	 * 
	 *  Nota por limitaciones en la implementacion del procedimiento de calculo de costo promedio
	 *  este pequeño parche/ajuste es requerido para evitar que algunos movimientos (Especialmente ventas)
	 *  queden sin costo en algun mes <b>POSTERIORES</b> al primer costo obtenido. (Ocurre normalmente en medidas especiales)
	 *   
	 * 
	 * @param year
	 */
	public void forwardCosto(final int year) {
		logger.info("fordwardCostos Trasladando cosos a POSTERIORES para YEAR:"+year);
		String sql="select distinct clave from sx_costos_p where year=? " +
		"and costop=0  ";
		final List<String> claves=getJdbcTemplate().queryForList(sql,new Object[]{year}, String.class);
		for(String clave:claves){
			forwardCosto(year,clave);
		}
	}
	
	public void forwardCosto(final int year,final String clave) {
		logger.info("fordwardCostos Trasladando cosos a POSTERIORES para YEAR:"+year+ " Prod:"+clave);
		
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<CostoPromedio> costos=session.createQuery(
				"from CostoPromedio c where c.year=? and c.clave=? order by c.mes")
				.setInteger(0, year)
				.setString(1, clave)
				.list();
				BigDecimal costop=BigDecimal.ZERO;
				for(CostoPromedio cp:costos){
					if(cp.getCostop().doubleValue()==0){							
						cp.setCostop(costop);
						System.out.println("Actualizando clave: "+clave+ "Mes:"+cp.getMes()+ "Costo: "+cp.getCostop());
					}else
						costop=cp.getCostop();
						
				}
				return null;
			}
			
		});
		
	}
	
	/**
	 * Verifica que una vez que exita costo promedio en un articulo este se
	 * traslade a meses <b>ANTERIORES</b> que no tengan costo
	 * 
	 *  Nota por limitaciones en la implementacion del procedimiento de calculo de costo promedio
	 *  este pequeño parche/ajuste es requerido para evitar que algunos movimientos (Especialmente ventas)
	 *  queden sin costo en algun mes <b>ANTERIORES</b> al primer costo obtenido. (Ocurre normalmente en medidas especiales)
	 *   
	 * 
	 * @param year
	 */
	public void backwardCosto(final int year) {
		logger.info("backwardCostos Trasladando cosos a ANTERIORES para YEAR:"+year);
		String sql="select distinct clave from sx_costos_p where year=? and costop=0  ";
		final List<String> claves=getJdbcTemplate().queryForList(sql,new Object[]{year}, String.class);
		for(String clave:claves){
			backwardCosto(year,clave);
		}	
	}
	
	public void backwardCosto(final int year,final String clave) {
		logger.info("backwardCostos Trasladando cosos a ANTERIORES para YEAR:"+year+ " Prod: "+clave);
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<CostoPromedio> costos=session.createQuery(
				"from CostoPromedio c where c.year=? and c.clave=? order by c.mes desc")
				.setInteger(0, year)
				.setString(1, clave)
				.list();
				BigDecimal costop=BigDecimal.ZERO;
				for(CostoPromedio cp:costos){
					if(cp.getCostop().doubleValue()==0){							
						cp.setCostop(costop);
						if(costop.doubleValue()>0){
							System.out.println("Actualizando clave: "+clave+ " Mes:"+cp.getMes()+ "Costo: "+cp.getCostop());
						}
						
					}else
						costop=cp.getCostop();					
				}
				return null;
			}	
		});
		
		
	}
	
	/**
	 * Localiza el costo promedio para todos los productos en el periodo indicado
	 * 
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedio> buscarCostosPromedios(final int year,int mes){
		return getDao().buscarCostosPromedios(year, mes);
	}
	
	/**
	 * Localiza el costo promedio para el producto en el periodo indicado
	 * 
	 * @param clave
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedio> buscarCostosPromedios(final String clave,final int year,int mes){
		return getDao().buscarCostosPromedios(clave, year, mes);
	}
	
	/**
	 * Busca los CostoPromedioItem
	 * 
	 * @param clave
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedioItem> buscarCostoItems(final String clave,final int year, final int mes){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/costo_promedio_item_rows.sql");
		sql=sql.replaceAll("@CLAVE", "\'"+clave+"\'");
		sql=sql.replaceAll("@YEAR", String.valueOf(year));
		sql=sql.replaceAll("@MONTH", String.valueOf(mes));
		if(logger.isDebugEnabled()){
			logger.debug(sql);			
		}
		int mes_ini=mes;
		int year_ini=year;
		if(mes==1){
			mes_ini=12;
			year_ini=year-1;
		}else{
			mes_ini=mes-1;
		}
		//System.out.println(sql);
		return getJdbcTemplate().query(sql,new Object[]{year_ini,mes_ini},new CostoPromedioItemMapper());
	}


	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	

	public ProductoManager getProductoManager() {
		return productoManager;
	}

	public void setProductoManager(ProductoManager productoManager) {
		this.productoManager = productoManager;
	}

	public static void main(String[] args) {
		DBUtils.whereWeAre();
		//int mes=2;
		//int year=2009;
		//com.luxsoft.siipap.service.ServiceLocator2.getCostosServices().actualizarCostoDeInventarioAPromedio(year, mes-1);
		
		//com.luxsoft.siipap.service.ServiceLocator2.getCostosServices().actualizarCostoDeInventarioAPromedio(year, mes);
		com.luxsoft.siipap.service.ServiceLocator2
		.getCostoPromedioManager()
		.actualizarCostoPromedio(2011, 1,"POL74");
		
	}

}
