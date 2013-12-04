package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeSols implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	SimpleJdbcInsert insertPartidas;
	
	
	public ImportadorCentralDeSols(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_SOLICITUD_TRASLADOS");
		insertPartidas=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_SOLICITUD_TRASLADOSDET");
		
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select SOL_ID from SX_SOLICITUD_TRASLADOS where date(fecha)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros detectados: "+rows.size()+ " En la sucursal: "+sucursalId+ " Fecha: "+fecha);
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(SolicitudDeTraslado.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Faltantes localizados: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_SOLICITUD_TRASLADOS where SOL_ID=?", new Object[]{id});
				
				pendientes.add(row);
			}
			int[] res=insert.executeBatch(pendientes.toArray(new Map[0]));
			logger.info("Insertados: "+res.length);
			importarPartidasFaltantes(fecha, template, sucursalId);
			
					
		}		
	}
	
	public void importarPartidasFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select SOL_ID from SX_SOLICITUD_TRASLADOSDET where SOL_ID in (select SOL_ID from SX_SOLICITUD_TRASLADOS where date(fecha)=?)";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros de Partidas detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				//Object found=ServiceLocator2.getHibernateTemplate().get(SolicitudDeTrasladoDet.class, id);
				//if(found==null)
					faltantes.add(id);
			}
			logger.info("Partidas faltantes localizadas: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			
			for(String id:faltantes){
				List<Map<String,Object>> roww=template.queryForList("select * from SX_SOLICITUD_TRASLADOSDET where SOL_ID=?", new Object[]{id});
				try {
					int[] res=insertPartidas.executeBatch(roww.toArray(new Map[0]));
					logger.info(" Partidas Insertados: "+res.length);
				} catch (Exception e) {}
			}
			
		}		
	}
	
	

}
