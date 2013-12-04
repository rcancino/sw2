package com.luxsoft.sw2.replica.valida;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.utils.LoggerHelper;

public class ExportadorDeSols {
	
	Logger logger=LoggerHelper.getLogger();
	
	public void exportar(Long sucursalId){
		exportar(new Date(),sucursalId);
	}
	
	public void exportar(String sfecha,Long sucursalId){
		exportar(DateUtil.toDate(sfecha),sucursalId);
	}
	
	public void exportar(Date fecha,Long sucursalId){
		
		String sql= " select * from SX_SOLICITUD_TRASLADOS where date(fecha)=? and sucursal_id=?";
		List<Map<String, Object>> rows=ServiceLocator2.getJdbcTemplate()
				.queryForList(sql,
						new Object[]{ValUtils.getPamaeter(fecha),sucursalId}
						);
		logger.info("Solicitudes registradas: "+rows.size()+ " Sucursal: "+sucursalId+" Fecha: "+fecha);
				
		
		for(Map<String,Object> row:rows){
			Long destino=(Long)row.get("ORIGEN_ID");
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(destino);
			SimpleJdbcInsert insert=new SimpleJdbcInsert(template).withTableName("SX_SOLICITUD_TRASLADOS");
			
			String solId=(String)row.get("SOL_ID");
			try {
				insert.execute(row);
				
				logger.info("Sol enviado: "+row+ "  a sucursal: "+sucursalId);
			} catch (Exception e) {
				logger.error("Error insertando Sol causa:"+ExceptionUtils.getRootCauseMessage(e));
			}
			exportarDetalles(template,solId);
		}
		
	}
	
	private void exportarDetalles(JdbcTemplate template,String sol_id){		
		String sql= " select * from SX_SOLICITUD_TRASLADOSDET where sol_id =?";
		List<Map<String, Object>> rows=ServiceLocator2.getJdbcTemplate()
				.queryForList(sql,new Object[]{sol_id}
						);		
		
		for(Map<String,Object> row:rows){
			SimpleJdbcInsert insert=new SimpleJdbcInsert(template).withTableName("SX_SOLICITUD_TRASLADOSDET");
			try {
				insert.execute(row);
				logger.info("Partida insertada: "+row);
			} catch (Exception e) {
				logger.error("Error insertando SolDet causa:"+ExceptionUtils.getRootCauseMessage(e));
			}
		}
	}
	
	public static void main(String[] args) {
		ExportadorDeSols exp=new ExportadorDeSols();
		exp.exportar("04/06/2012",5L);
	}

}
