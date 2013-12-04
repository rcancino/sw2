package com.luxsoft.siipap.inventarios.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.inventarios.procesos.AjusteDeCostoParaTransformaciones;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.SQLUtils;

/**
 * NOTA: Facade para los diversos servicios de costeo de inventario
 * 
 * 
 *   Actualizar las existencias ejecutando InventarioAnualDaoImpl
 *   CostosServices.actualizarCostoPromedio2
 *   AjustarCostoPromedio.execute
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CostosServices extends HibernateDaoSupport{
	
	private ExistenciaDao existenciaDao;
	
	private CostoPromedioManager costoPromedioManager;
	
	private CostoUltimoManager costoUltimoManager;
	
	private TransformacionesManager transformacionesManager;
	
	private JdbcTemplate jdbcTemplate;
	
	private Logger logger=Logger.getLogger(getClass());
	
	
	/**
	 * Actualiza los costos promedio para el año mes y producto indicado	
	 *    
	 *    Actualiza los saldos iniciales del mes
	 *    Calcula el costo promedio     
	 *    Actualiza el costo de las existencias
	 *    Costea los movimientos 	
	 *    Actualiza el costo de las transformaciones  
 	 * 
	 * @param year
	 * @param mes
	 */
	public void actualizarCostosPromedio(String clave,int year, int mes){
		
		logger.info("Actualizando costos Producto: "+clave+ " Periodo: "+mes+ "/ "+year);		
		
		AjusteDeCostoParaTransformaciones transofor=new AjusteDeCostoParaTransformaciones();
		transofor.actualizarCostoOrigen(clave,year, mes);
		
		getCostoPromedioManager().actualizarCostoPromedio(year, mes,clave);		
		
		//getExistenciaDao().actualizarExistencias(clave,year, mes);
		
		actualizarMovimientosPromedio(clave,year, mes);	
		actualizarCostoEnExistencias(clave, year, mes);
		
	}
	
	
	/**
	 * Actualiza los costos para el año mes indicado	   
	 *   
	 *   
	 *    Calcula el costo promedio     
	 *    Actualiza el costo de las existencias
	 *    Costea los movimientos 	
	 *    Actualiza el costo de las transformaciones 		  
 	 * 
	 * @param year
	 * @param mes
	 */
	public void actualizarCostosAPromedio(int year, int mes){
		
		logger.info("Actualizando costos: "+year+ " - "+mes);
		AjusteDeCostoParaTransformaciones transofor=new AjusteDeCostoParaTransformaciones();
		transofor.actualizarCostoOrigen(year, mes);
		
		actualizarCostoPromedio(year, mes);
			
		getCostoPromedioManager().backwardCosto(year);
		
		actualizarMovimientosPromedio(year, mes);
		
		actualizarCostoDeInventarioAPromedio(year, mes);
		
	}
	
	
	
	/**
	 * Actualiza el costo de los movimientos de inventario
	 * 
	 * Asume que los costos promedios y ultimos ya existen en la tabla
	 * de SX_COSTOS_P
	 *  
	 * @param year
	 * @param mes
	 */
	public void actualizarMovimientosPromedio(int year,int mes){
		logger.info("Actualizando movimientos de inventarios del periodo: "+year+ "/"+mes+ " a COSTO PROMEDIO");
		
		final String sq= " update @TABLA x set x.costop=ifnull((select a.costop from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0) where year(x.fecha)=? and month(x.fecha)=?";
						
		String[] tablas={
				"SX_VENTASDET"
				,"SX_INVENTARIO_MOV"
				,"SX_INVENTARIO_DEV"
				,"SX_INVENTARIO_KIT"
				,"SX_INVENTARIO_TRS"
				,"SX_INVENTARIO_TRD"
				,"SX_INVENTARIO_COM"
				,"SX_INVENTARIO_MAQ"
				,"SX_INVENTARIO_DEC"
				};
		
		for(String tabla:tablas){			
			String sql=sq.replaceAll("@TABLA", tabla);
			logger.info("Actualizando: "+tabla);
			int res=getJdbcTemplate().update(sql, new Object[]{year,mes,year,mes});
			logger.info("Costos promedios actualizados: "+res);
		}
	}
	
	public void actualizarMovimientosPromedio(String clave,int year,int mes){
		logger.info("Actualizando movimientos de inventarios del periodo: "+clave+"  "+year+ "/"+mes+ " a COSTO PROMEDIO");
		
		final String sq= " update @TABLA x " +
				" set x.costop=ifnull((select a.costop from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0) " +
				" where year(x.fecha)=? and month(x.fecha)=? and x.clave=?";
				
						
		String[] tablas={
				"SX_VENTASDET"
				,"SX_INVENTARIO_MOV"
				,"SX_INVENTARIO_DEV"
				,"SX_INVENTARIO_KIT"
				,"SX_INVENTARIO_TRS"
				,"SX_INVENTARIO_TRD"
				,"SX_INVENTARIO_COM"
				,"SX_INVENTARIO_MAQ"
				,"SX_INVENTARIO_DEC"
				};
		
		for(String tabla:tablas){			
			String sql=sq.replaceAll("@TABLA", tabla);
			logger.info("Actualizando: "+tabla);
			int res=getJdbcTemplate().update(sql, new Object[]{year,mes,year,mes,clave});
			logger.info("Costos promedios actualizados: "+res);
		}
	}
	
	/**
	 * Actualiza el costo de los movimientos de inventario a ULTIMO COSTO
	 * 
	 * Asume que los costos  ultimos ya existen en la tabla
	 * de SX_COSTOS_P
	 *  
	 * @param year
	 * @param mes
	 */
	public void actualizarMovimientosUltimo(int year,int mes){
		logger.info("Actualizando movimientos de inventarios del periodo: "+year+ "/"+mes+ " a COSTO ULTIMO ");
		final String sq=" update @TABLA x set x.costou=ifnull((select a.costou from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0) where year(x.fecha)=? and month(x.fecha)=?";
				
		String[] tablas={
				"SX_VENTASDET"
				,"SX_INVENTARIO_MOV"
				,"SX_INVENTARIO_DEV"
				,"SX_INVENTARIO_KIT"
				,"SX_INVENTARIO_TRS"
				,"SX_INVENTARIO_TRD"
				,"SX_INVENTARIO_COM"
				,"SX_INVENTARIO_MAQ"
				,"SX_INVENTARIO_DEC"
				};
		
		for(String tabla:tablas){			
			String sql=sq.replaceAll("@TABLA", tabla);
			logger.info("Actualizando: "+tabla);
			int res=getJdbcTemplate().update(sql, new Object[]{year,mes,year,mes});
			logger.info("Costos promedios actualizados: "+res);
		}
	}
	
	public void actualizarMovimientosUltimo(String clave,int year,int mes){
		logger.info("Actualizando movimientos de inventarios del periodo: "+clave+"  "+year+ "/"+mes+ " a COSTO ULTIMO ");
		final String sq=" update @TABLA x " +
				" set x.costou=ifnull((select a.costou from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0) " +
				" where year(x.fecha)=? and month(x.fecha)=? and x.clave=?";
				
				
		String[] tablas={
				"SX_VENTASDET"
				,"SX_INVENTARIO_MOV"
				,"SX_INVENTARIO_DEV"
				,"SX_INVENTARIO_KIT"
				,"SX_INVENTARIO_TRS"
				,"SX_INVENTARIO_TRD"
				,"SX_INVENTARIO_COM"
				,"SX_INVENTARIO_MAQ"
				,"SX_INVENTARIO_DEC"
				};
		
		for(String tabla:tablas){			
			String sql=sq.replaceAll("@TABLA", tabla);
			logger.info("Actualizando: "+tabla);
			int res=getJdbcTemplate().update(sql, new Object[]{year,mes,year,mes,clave});
			logger.info("Costos promedios actualizados: "+res);
		}
	}
	
	/**
	 * Actualiza el costo del inventario (SX_EXISTENCIAS)  a costo promedio
	 * para año y mes indicado
	 *  
	 * @param year
	 * @param mes
	 */
	public void actualizarCostoDeInventarioAPromedio(int year,int mes){
		logger.info("Actualizando existencias  del periodo: "+year+ "/"+mes + "  (Costo del inventario)");
		String sql="update SX_EXISTENCIAS x " +
				"    set x.costop=ifnull((select a.costop from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0) " +
				"	,x.costo=ifnull((select a.costop from sx_costos_p a where a.clave=x.clave and a.year=x.year and a.mes=x.mes),0) " +
				"	where x.year=? and x.mes=?";		
		int res=getJdbcTemplate().update(sql,new Object[]{year,mes,year,mes});
		logger.info("Inventario a costo promedio actualizado: "+res);
	}
	
	/**
	 * Actualiza el costo en la tabla de existencias, lo toma de la tabla de sx_costos_p
	 * 
	 * @param clave
	 * @param year
	 * @param mes
	 */
	private void actualizarCostoEnExistencias(final String clave,int year,int mes){
		logger.info("Actualizando existencias  del periodo: "+ clave+"  "+year+ "/"+mes + "  (Costo del inventario)");
		String sql="update SX_EXISTENCIAS x " +
		"    set x.costop=ifnull((select a.costop from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0) " +
		"	,x.costo=ifnull((select a.costop from sx_costos_p a where a.clave=x.clave and a.year=x.year and a.mes=x.mes),0) " +
		"	where x.year=? and x.mes=? and x.clave=?";
		int res=getJdbcTemplate().update(sql, new Object[]{year,mes,year,mes,clave});
		logger.info("Inventario a costo promedio actualizado: "+res);
	}
	
	/**
	 * Actualiza el costo del inventario (SX_EXISTENCIAS) a costo ultimo
	 *  
	 * @param year
	 * @param mes
	 */
	public void actualizarCostoDeInventarioAUltimo(int year,int mes){
		logger.info("Actualizando existencias del periodo: "+year+ "/"+mes+ "  (Costo del inventario Costo Promedio)");
		final String sql=" update SX_EXISTENCIAS x set x.costou=ifnull((select a.costou from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0)" +
		"  where x.mes=? and x.year=?";
		int res=getJdbcTemplate().update(sql, new Object[]{year,mes,year,mes});
		logger.info("Inventario a ultimo costo actualizado: "+res);
	}
	
	public void actualizarCostoDeInventarioAUltimo(String clave,int year,int mes){
		logger.info("Actualizando existencias del periodo: "+year+ "/"+mes+ "  (Costo del inventario Ultimo Costo)");
		final String sql=" update SX_EXISTENCIAS x set x.costou=ifnull((select a.costou from sx_costos_p a where a.clave=x.clave and a.year=? and a.mes=?),0)" +
		"  where x.mes=? and x.year=? and x.clave=?";				
		int res=getJdbcTemplate().update(sql, new Object[]{year,mes,year,mes,clave});
		logger.info("Inventario a ultimo costo actualizado: "+res);
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public ExistenciaDao getExistenciaDao() {
		return existenciaDao;
	}
	public void setExistenciaDao(ExistenciaDao existenciaDao) {
		this.existenciaDao = existenciaDao;
	}

	public CostoPromedioManager getCostoPromedioManager() {
		return costoPromedioManager;
	}

	public void setCostoPromedioManager(CostoPromedioManager costoPromedioManager) {
		this.costoPromedioManager = costoPromedioManager;
	}	
	

	public CostoUltimoManager getCostoUltimoManager() {
		return costoUltimoManager;
	}

	public void setCostoUltimoManager(CostoUltimoManager costoUltimoManager) {
		this.costoUltimoManager = costoUltimoManager;
	}

	

	public TransformacionesManager getTransformacionesManager() {
		return transformacionesManager;
	}


	public void setTransformacionesManager(
			TransformacionesManager transformacionesManager) {
		this.transformacionesManager = transformacionesManager;
	}
	
	/**
	 * Actualiza el costo 
	 * 
	 * @param year
	 * @param mes
	 */
	public void actualizarCostoPromedio(final int year,final int mes){
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/costo_promedio_all.sql");
		sql=sql.replaceAll("@YEAR", String.valueOf(year));
		sql=sql.replaceAll("@MONTH", String.valueOf(mes));
		
		logger.info("Actualizando Costo Promedio para: "+mes+"/"+year);		
		int mes_ini=mes;
		int year_ini=year;
		if(mes==1){
			mes_ini=12;
			year_ini=year-1;
		}else{
			mes_ini=mes-1;
		}
		System.out.println(sql);
		List<Map<String,Object>> rows=getJdbcTemplate().queryForList(sql,new Object[]{year_ini,mes_ini});
		//logger.info("Registros a procesar: "+rows.size());
		
		//Agrupamos los registros en listas por producto
		EventList<Map<String,Object>> source=GlazedLists.eventList(rows);
		GroupingList<Map<String,Object>> groupList=new GroupingList<Map<String,Object>>(source,new Comparator<Map<String,Object>>(){
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String a1=o1.get("CLAVE").toString();
				String a2=o2.get("CLAVE").toString();
				return a1.compareTo(a2);
			}
		});
		
		//Procesamos cada lista
		for(List<Map<String,Object>> list:groupList){
			
			String clave=(String)list.get(0).get("CLAVE");			
			CostoPromedio cp=getCostoPromedioManager().buscarCostoPromedio(year, mes, clave);
			if(cp==null){
				Producto prod=ServiceLocator2.getProductoManager().buscarPorClave(clave);
				//Assert.notNull(prod,"No existe el producto: "+clave);
				if(prod==null){
					System.out.println("No existe el producto: "+clave);
					continue;
				}
				cp=new CostoPromedio(year,mes,prod);
			}
			
			BigDecimal cantidadTotal=BigDecimal.ZERO;
			CantidadMonetaria importeTotal=CantidadMonetaria.pesos(0);
			
			BigDecimal cantidadInicial=BigDecimal.ZERO;
			CantidadMonetaria importeInicial=CantidadMonetaria.pesos(0);
			
			for(Map<String,Object> row:list){
				
				/*Number can=(Number)row.get("CANTIDAD");
				cantidadTotal=cantidadTotal.add(BigDecimal.valueOf(can.doubleValue()));
				Number costo=(Number)row.get("COSTO");
				CantidadMonetaria importe=CantidadMonetaria.pesos(costo.doubleValue());
				importe=importe.multiply(can.doubleValue());
				importesTotal=importesTotal.add(importe);*/
				
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
			cp=getCostoPromedioManager().save(cp);
			
		}
	}
	
	public static void main(String[] args) throws IOException {
		DBUtils.whereWeAre();
		//ServiceLocator2.getCostosServices().actualizarCostosAPromedio(2010, 4);
		//ServiceLocator2.getCostosServices().actualizarCostosPromedio("CRB9095516", 2010, 2);
		//ServiceLocator2.getCostosServices().actualizarCostoPromedio(2009, 1);
		//ServiceLocator2.getCostosServices().actualizarCostosPromedio("S/BA4090RD", 2011, 1);
		for(int mes=1;mes<=7;mes++){
			//ServiceLocator2.getCostosServices().actualizarCostosPromedio("CRB9095516", 2010, mes);
			//ServiceLocator2.getCostosServices().actualizarMovimientosPromedio(2009, mes);
			//ServiceLocator2.getCostosServices().actualizarCostoDeInventarioAPromedio(2009, mes);
		}
		//ServiceLocator2.getExistenciaDao().actualizarExistencias("CAP507016", 2009, 7);
		//AjusteDeCostoParaTransformaciones transofor=new AjusteDeCostoParaTransformaciones();
		//transofor.actualizarCostoOrigen(2010, 3);
		
		ServiceLocator2.getCostosServices().actualizarCostosAPromedio(2012,6);
		//ServiceLocator2.getCostosServices().actualizarCostosPromedio("R4MB141", 2011, 11);
	}
	

}
