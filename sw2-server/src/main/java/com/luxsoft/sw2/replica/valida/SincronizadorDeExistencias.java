package com.luxsoft.sw2.replica.valida;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.utils.LoggerHelper;

public class SincronizadorDeExistencias {
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	private boolean todo=false;
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	
	public SincronizadorDeExistencias(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_EXISTENCIAS");
	}
	
	public void sincronizar(){
		sincronizar(new Date());
	}
	
	public void sincronizarFaltantes(){
		todo=false;
		sincronizar(new Date());
	}
	
	public void sincronizarTodo(){
		todo=true;
		sincronizar(new Date());
	}
	
	public void sincronizar(Date fecha){
		for(Long sucursalId:sucursales){
			importarFaltantes(fecha,  sucursalId);
		}
	}
	
	public void importarFaltantes(final Date fecha,Long sucursalId){
		if(todo){
			importarTodo(fecha, sucursalId);
		}else{
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
			String sql="SELECT * FROM SX_EXISTENCIAS where date(modificado)=? and SUCURSAL_ID=?";
			Object[] args=new Object[]{ValUtils.getPamaeter(fecha),sucursalId};
			List<Map<String,Object>> rows=template.queryForList(sql,args);
			updateLog("TX_IMPORTADO", rows.toArray(new Map[0]));
			for(Map<String,Object> row:rows){
				try {
					ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_EXISTENCIAS WHERE INVENTARIO_ID=?", new Object[]{row.get("INVENTARIO_ID")});
					insert.execute(row);
					logger.info("Exis actualizado: "+row);
				} catch (Exception e) {
					logger.error("Error importando exis: "+row+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
				}
			}
			exportarFaltantes(rows, fecha, sucursalId);
		}				
	}
	
	public void exportarFaltantes(List<Map<String,Object>> rows,Date fecha,Long sucursalOrigen){
		
		for(Long sucursalId:sucursales){
			try {
				if(sucursalId.equals(sucursalOrigen))
					continue;
				JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
				final SimpleJdbcInsert sucInser=new SimpleJdbcInsert(template).withTableName("SX_EXISTENCIAS");
				for(Map<String,Object> row:rows){
					template.update("DELETE FROM SX_EXISTENCIAS WHERE INVENTARIO_ID=?", new Object[]{row.get("INVENTARIO_ID")});
					updateLog("TX_REPLICADO", row);
					sucInser.execute(row);
					logger.info("Exis actualizada para: "+row.get("CLAVE")+ " sucursal: "+sucursalId+ " De la sucursal: "+sucursalOrigen);
				}	
			} catch (Exception e) {
				logger.error("Error exportando existencias de la sucursal: "+sucursalOrigen+ " a la sucursal: "+sucursalId+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	private void importarTodo(final Date fecha,Long sucursalId){
		
		//Eliminar en Oficinas
		Object[] args=new Object[]{Periodo.obtenerYear(fecha),Periodo.obtenerMes(fecha)+1,sucursalId};
		int res=ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_EXISTENCIAS WHERE YEAR=? AND MES=? AND SUCURSAL_ID=?", args);
		logger.info("Existencias eliminadas: "+res);
		
		JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
		String sql="SELECT * FROM SX_EXISTENCIAS where YEAR=? AND MES=? AND SUCURSAL_ID=?";
		List<Map<String,Object>> rows=template.queryForList(sql,args);
		logger.info("Existencias detectadas : "+rows.size()+ " En la sucursal: "+sucursalId+ " Fecha: "+fecha);
		updateLog("TX_IMPORTADO", rows.toArray(new Map[0]));
		int[] ins=insert.executeBatch(rows.toArray(new Map[0]));
		logger.info("Existencias insertadas en oficinas: "+ins.length+ " desde la sucursal: "+sucursalId);
		
		exportarTodo(rows, fecha, sucursalId);
		
	}
	
	public void exportarTodo(List<Map<String,Object>> rows,Date fecha,Long sucursalOrigen){
		for(Long sucursalId:sucursales){
			try {
				if(sucursalId.equals(sucursalOrigen))
					continue;
				Object[] args=new Object[]{Periodo.obtenerYear(fecha),Periodo.obtenerMes(fecha)+1,sucursalOrigen};
				JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
				int res=template.update("DELETE FROM SX_EXISTENCIAS WHERE YEAR=? AND MES=? AND SUCURSAL_ID=?", args);
				logger.info("Existencias eliminadas: "+res+ " en sucursal: "+sucursalId);
				updateLog("TX_REPLICADO", rows.toArray(new Map[0]));
				final SimpleJdbcInsert sucInser=new SimpleJdbcInsert(template).withTableName("SX_EXISTENCIAS");
				int[] ins=sucInser.executeBatch(rows.toArray(new Map[0]));
				logger.info("Existencias insertadas :"+ins.length+ " en sucursal: "+sucursalId);
			} catch (Exception e) {
				logger.error("Error exportando existencias de la sucursal: "+sucursalOrigen+ " a la sucursal: "+sucursalId+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	private void updateLog(String col,Map<String,Object>...rows){
		final Date time=new Date();
		for(Map<String,Object> row:rows){
			row.put(col, time);
		}
	}
	
	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public SincronizadorDeExistencias addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}

	public boolean isTodo() {
		return todo;
	}

	public SincronizadorDeExistencias setTodo(boolean todo) {
		this.todo = todo;
		return this;
	}
	
	public static void main(String[] args) {
		new SincronizadorDeExistencias()
		.addSucursal(2L,3L,5L,6L,9L)
		//.setTodo(true)
		//.sincronizar(DateUtil.toDate("31/05/2012"));
		.sincronizarFaltantes();
	}
	

}
