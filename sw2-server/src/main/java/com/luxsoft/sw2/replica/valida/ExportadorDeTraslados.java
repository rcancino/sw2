package com.luxsoft.sw2.replica.valida;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.utils.LoggerHelper;

public class ExportadorDeTraslados {
	
	Logger logger=LoggerHelper.getLogger();
	
	public void exportar(Long sucursalId){
		exportar(new Date(),sucursalId);
	}
	
	public void exportar(String sfecha,Long sucursalId){
		exportar(DateUtil.toDate(sfecha),sucursalId);
	}
	
	public void exportar(Date fecha,Long sucursalId){
		
		String sql= " select a.traslado_id as TRASLADO_ID,b.sol_id,b.fecha,a.sucursal_id as atendio,b.sucursal_id as solicito" 
					+" from sx_traslados a join sx_solicitud_traslados b on a.sol_id=b.sol_id "
					+" where date(a.fecha)=? and b.sucursal_id=?";
		List<Map<String, Object>> rows=ServiceLocator2.getJdbcTemplate()
				.queryForList(sql,
						new Object[]{ValUtils.getPamaeter(fecha),sucursalId}
						);
		
		SimpleJdbcInsert insert=new SimpleJdbcInsert(ConnectionServices.getInstance().getJdbcTemplate(sucursalId)).withTableName("SX_TRASLADOS");
		SimpleJdbcInsert insertPartidas=new SimpleJdbcInsert(ConnectionServices.getInstance().getJdbcTemplate(sucursalId)).withTableName("SX_INVENTARIO_TRD");
		
		for(Map<String,Object> row:rows){
			
			String traslado_id=(String)row.get("TRASLADO_ID");
			Map<String,Object> traslado=ServiceLocator2.getJdbcTemplate().queryForMap("select * from sx_traslados where traslado_id=?",new Object[]{traslado_id});
			List<Map<String,Object>> partidas=ServiceLocator2.getJdbcTemplate().queryForList("select * from SX_INVENTARIO_TRD where traslado_id=?",new Object[]{traslado_id});
			try {
				insert.execute(traslado);
				logger.info("Traslado enviado: "+traslado+ "  a sucursal: "+sucursalId);
				
			} catch (Exception e) {
				logger.error("Error insertando traslado causa:"+ExceptionUtils.getRootCauseMessage(e));
			}
			try {
				insertPartidas.executeBatch(partidas.toArray(new  Map[0]));
			} catch (Exception e) {
				logger.error("Error insertando traslado causa:"+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	public static void main(String[] args) {
		ExportadorDeTraslados exp=new ExportadorDeTraslados();
		exp.exportar("04/06/2012",5L);
	}

}
