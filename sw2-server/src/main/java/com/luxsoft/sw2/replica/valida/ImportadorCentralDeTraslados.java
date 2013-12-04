package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeTraslados implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	SimpleJdbcInsert insertPartidas;
	
	
	public ImportadorCentralDeTraslados(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_TRASLADOS");
		insertPartidas=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_INVENTARIO_TRD");
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
				
		String sql="select * from SX_TRASLADOS where date(fecha)=?";
		List<Map<String, Object>> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha));
		logger.info("Registros detectados: "+rows.size());
		if(!rows.isEmpty()){
			
			for(Map<String,Object> row:rows){
				try {
					int res=insert.execute(row);
					if(res>0)
						logger.info("Traslados Insertados: "+row);
				} catch (Exception e) {}				
			}
			
			importarPartidasFaltantes(fecha, template, sucursalId);
			
			
		}		
	}
	
	public void importarPartidasFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select * from SX_INVENTARIO_TRD where date(fecha)=?";
		List<Map<String, Object>> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha));
		logger.info("Registros de Partidas detectados: "+rows.size());
		if(!rows.isEmpty()){			
			for(Map<String,Object> row:rows){
				try {
					int res=insertPartidas.execute(row);
					if(res>0)
						logger.info("Partida insertada: "+row);
				} catch (Exception e) {
					
				}
			}
			
			
		}	
		
	}

}
