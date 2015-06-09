package com.luxsoft.sw3.bi;

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
import com.luxsoft.siipap.swing.utils.MessageUtils;
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

	
	
	
	public void actualizarExistenciasOficinas(Date fecha){
		  //Long[] sucursales={2L,3L,5L,6L,9L,11L};
	
		for(Long sucursalId:sucursales){
			
	 System.out.println("Validando Existencias para Sucursal: "+sucursalId +" Del Dia :" +fecha);
			actualizarExistenciasOficinas(fecha, sucursalId);
			
		}
		
	}

	public void actualizarExistenciasOficinas(final Date fecha,Long sucursalId){
		
		JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);

		String sqlSucursales="SELECT NOMBRE FROM SW_SUCURSALES WHERE SUCURSAL_ID NOT IN (?,1,12,4,8,10,7) ";
		Object[] arguments=new Object[]{sucursalId};
		List<Map<String,Object>> sucs=ServiceLocator2.getJdbcTemplate().queryForList(sqlSucursales, arguments);



		String sql="SELECT * FROM SX_EXISTENCIAS where date(modificado)=? and SUCURSAL_ID=?";
		Object[] args=new Object[]{ValUtils.getPamaeter(fecha),sucursalId};
		List<Map<String,Object>> rows=template.queryForList(sql,args);
		//updateLog("TX_IMPORTADO", rows.toArray(new Map[0]));
		for(Map<String,Object> row:rows){
			try {
				String exiSuc=(String)row.get("INVENTARIO_ID");
				Double cantSuc=(Double)row.get("CANTIDAD");

				String sqlLocal="SELECT * FROM SX_EXISTENCIAS WHERE INVENTARIO_ID=?";
				Object[] argumentos=new Object[]{exiSuc};

				//Map<String,Object> rowOfi=ServiceLocator2.getJdbcTemplate().queryForMap(sqlLocal, argumentos);
				List<Map<String,Object>> rowOfi=ServiceLocator2.getJdbcTemplate().queryForList(sqlLocal, argumentos);
				if(rowOfi.isEmpty()|| rowOfi==null){ 
					System.out.println("No existe registro insertando:  "+row.get("INVENTARIO_ID"));
					//System.out.println("'"+row.get("INVENTARIO_ID")+"', --INSERT "+sucursalId);
					insert.execute(row);
					for(Map<String,Object> sucursal:sucs){
						String sucur=(String)	sucursal.get("NOMBRE");
						//						logger.info("AuditLog para INSERT : "+sucur);

						String inserAuditIns="INSERT INTO AUDIT_LOG (entityId,entityName,action,tableName,ip,SUCURSAL_ORIGEN,SUCURSAL_DESTINO,dateCreated,lastUpdated,replicado,message,version)" +
								"  VALUES (?,\'Existencia\',\'INSERT\',\'SX_EXISTENCIAS\',\'10.10.1.1\',\'OFICINAS\',?,now(),now(),null,\'\',0)";
						Object[] argsAuditIns=new Object[]{row.get("INVENTARIO_ID"),sucur};
						ServiceLocator2.getJdbcTemplate().update(inserAuditIns, argsAuditIns);
					}
				}else{
					Double cantOfi=(Double) rowOfi.get(0).get("CANTIDAD");
					if(!cantOfi.equals(cantSuc)){

					//	logger.info("Hay que actualizar:"+exiSuc +" Por que La cantidad de Oficinas: "+cantOfi + " Es diferente a la cantidad de sucursal: "+ cantSuc);
						System.out.println("Actualizando:  " +row.get("INVENTARIO_ID"));
					//	System.out.println("'"+row.get("INVENTARIO_ID")+"', --UPDATE " +sucursalId);
						String update="UPDATE SX_EXISTENCIAS SET CANTIDAD=?, RECORTE=?, RECORTE_COMENTARIO=?, RECORTE_FECHA=?, MODIFICADO=? WHERE INVENTARIO_ID=?";
						Object[] argsUpdate=new Object[]{row.get("CANTIDAD"),row.get("RECORTE"),row.get("RECORTE_COMENTARIO"),row.get("RECORTE_FECHA"),row.get("MODIFICADO"),row.get("INVENTARIO_ID")};
						ServiceLocator2.getJdbcTemplate().update(update, argsUpdate);
						for(Map<String,Object> sucursal:sucs){

							String sucur=(String)	sucursal.get("NOMBRE");
							//							logger.info("AuditLog para UPDATE : "+sucur);
							String inserAuditUp="INSERT INTO AUDIT_LOG (entityId,entityName,action,tableName,ip,SUCURSAL_ORIGEN,SUCURSAL_DESTINO,dateCreated,lastUpdated,replicado,message,version)" +
									"  VALUES (?,\'Existencia\',\'UPDATE\',\'SX_EXISTENCIAS\',\'10.10.1.1\',\'OFICINAS\',?,now(),now(),null,\'\',0)";
							Object[] argsAuditUp=new Object[]{row.get("INVENTARIO_ID"),sucur};
							ServiceLocator2.getJdbcTemplate().update(inserAuditUp, argsAuditUp);
						}

					}
				}

				/*logger.info("Exis actualizado Suc: "+row);
					logger.info("Exis actualizado Ofi: "+rowOfi);*/
			} catch (Exception e) {
				System.out.println("Error importando exis: "+row+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}

		}
		

	}
	
	
	
	
	
	public static void main(String[] args) {
		new SincronizadorDeExistencias()
		.addSucursal(2L,3L,5L,6L,9L,11L)
		//.actualizarExistenciasOficinas(DateUtil.toDate("14/02/2014"));
		//.actualizarExistenciasOficinas(new Date());
		.actualizarExistenciasOficinas(new Date("2015/06/08"));
		
		
		
	}
	

}
