package com.luxsoft.sw2.replica.valida;

import java.text.MessageFormat;
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
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.utils.LoggerHelper;

public class SincronizadorDeExistencias2 {
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	
	public SincronizadorDeExistencias2(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_EXISTENCIAS");
	}
	
	public void sincronizar(){
		sincronizar(new Date());
	}
	
	public void sincronizar(Date fecha){
		for(Long sucursalId:sucursales){
			DBUtils.whereWeAre();
			importar(fecha,  sucursalId);
		}
	}
	
	public void sincronizarMesActual(){
		for(Long sucursalId:sucursales){
			importarMesActual(sucursalId);
		}
	}
	
	public void sincronizar(Periodo periodo){
		for(Long sucursalId:sucursales){
			DBUtils.whereWeAre();
			importar(periodo,  sucursalId);
		}
	}
	
	public void importar(final Date fecha,Long sucursalId){
		JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
		String sql="SELECT * FROM SX_EXISTENCIAS where date(modificado)=? and SUCURSAL_ID=?";
		Object[] args=new Object[]{ValUtils.getPamaeter(fecha),sucursalId};
		List<Map<String,Object>> rows=template.queryForList(sql,args);
		updateLog("TX_IMPORTADO", rows.toArray(new Map[0]));
		for(Map<String,Object> row:rows){
			try {
				String UPDATE="UPDATE  SX_EXISTENCIAS SET CANTIDAD=?,RECORTE=?,RECORTE_FECHA=?,RECORTE_COMENTARIO=?,MODIFICADO=? WHERE INVENTARIO_ID=?";
				Object[] params=new Object[]{
						row.get("CANTIDAD"),
						row.get("RECORTE"),
						row.get("RECORTE_FECHA"),
						row.get("RECORTE_COMENTARIO"),
						row.get("MODIFICADO"),
						row.get("INVENTARIO_ID")
				};
				int updated=ServiceLocator2.getJdbcTemplate().update(UPDATE,params );
				if(updated==0){
					logger.info("Exis insertada: "+row);
					insert.execute(row);
				}else
					logger.info("Exis actualizado: "+row.get("CLAVE")+"  Cantidad:"+row.get("CANTIDAD"));
			} catch (Exception e) {
				logger.error("Error importando exis: "+row+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}
		}
		exportar(rows,  sucursalId);			
	}
	
	public void exportar(List<Map<String,Object>> rows,Long sucursalOrigen){
		for(Long sucursalId:sucursales){
			try {
				if(sucursalId.equals(sucursalOrigen))
					continue;
				JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
				final SimpleJdbcInsert sucInser=new SimpleJdbcInsert(template).withTableName("SX_EXISTENCIAS");
				logger.info(MessageFormat.format("Exportando {0} registros de sucursal: {1}",rows.size(),sucursalId));
				for(Map<String,Object> row:rows){
					String UPDATE="UPDATE  SX_EXISTENCIAS SET CANTIDAD=?,RECORTE=?,RECORTE_FECHA=?,RECORTE_COMENTARIO=?,MODIFICADO=? WHERE INVENTARIO_ID=?";
					Object[] params=new Object[]{
							row.get("CANTIDAD"),
							row.get("RECORTE"),
							row.get("RECORTE_FECHA"),
							row.get("RECORTE_COMENTARIO"),
							row.get("MODIFICADO"),
							row.get("INVENTARIO_ID")
					};
					updateLog("TX_REPLICADO", row);
					int updated=template.update(UPDATE, params);
					if(updated==0){
						sucInser.execute(row);
						logger.info("Nueva existencia generada: "+row.get("CLAVE")+ " sucursal: "+sucursalId+ " De la sucursal: "+sucursalOrigen);
					}else{
						logger.debug("Exis actualizada: "+row.get("CLAVE")+ " sucursal: "+sucursalId+ " De la sucursal: "+sucursalOrigen);
					}
				}	
			} catch (Exception e) {
				logger.error("Error exportando existencias de la sucursal: "+sucursalOrigen+ " a la sucursal: "+sucursalId+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}
		}
	}
	
	public void importarMesActual(Long sucursalId){
		importar(Periodo.periodoDeloquevaDelMes(),sucursalId);
	}
	
	public void importar(final Periodo periodo,Long sucursalId){
		JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
		String sql="SELECT * FROM SX_EXISTENCIAS where date(modificado) between ?  and ? and SUCURSAL_ID=?";
		Object[] args=new Object[]{
				ValUtils.getPamaeter(periodo.getFechaInicial()),
				ValUtils.getPamaeter(periodo.getFechaFinal()),
				sucursalId
				};
		List<Map<String,Object>> rows=template.queryForList(sql,args);
		logger.info(MessageFormat.format("Importando {0} registros de sucursal: {1}",rows.size(),sucursalId));
		updateLog("TX_IMPORTADO", rows.toArray(new Map[0]));
		for(Map<String,Object> row:rows){
			try {
				String UPDATE="UPDATE  SX_EXISTENCIAS SET CANTIDAD=?,RECORTE=?,RECORTE_FECHA=?,RECORTE_COMENTARIO=?,MODIFICADO=? WHERE INVENTARIO_ID=?";
				Object[] params=new Object[]{
						row.get("CANTIDAD"),
						row.get("RECORTE"),
						row.get("RECORTE_FECHA"),
						row.get("RECORTE_COMENTARIO"),
						row.get("MODIFICADO"),
						row.get("INVENTARIO_ID")
				};
				int updated=ServiceLocator2.getJdbcTemplate().update(UPDATE,params );
				if(updated==0){
					logger.info("Generando nueva existencia: "+row);
					insert.execute(row);
				}else{
					logger.debug("Exis actualizado: "+row.get("CLAVE")+"  Cantidad:"+row.get("CANTIDAD"));
				}
			} catch (Exception e) {
				logger.error("Error importando exis: "+row+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}
		}
		exportar(rows,  sucursalId);			
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
	
	public SincronizadorDeExistencias2 addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}

	
	
	public static void main(String[] args) {
		new SincronizadorDeExistencias2()
		.addSucursal(2L,3L,5L,6L,9L,11L,12L,13L,14L)
		//.addSucursal(3L,2l)
		//.sincronizar();
		.sincronizarMesActual();
	}
	
}
