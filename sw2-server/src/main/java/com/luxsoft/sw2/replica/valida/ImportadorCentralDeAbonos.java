package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeAbonos implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	SimpleJdbcInsert insertPartidas;
	SimpleJdbcInsert insertBorrados;
	
	public ImportadorCentralDeAbonos(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_CXC_ABONOS");
		insertPartidas=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_CXC_APLICACIONES");
		insertBorrados=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_CXC_ABONOS_BORRADOS");
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select ABONO_ID from SX_CXC_ABONOS where date(fecha)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(Abono.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Faltantes localizados: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_CXC_ABONOS where ABONO_ID=?", new Object[]{id});
				
				pendientes.add(row);
			}
			for(Map<String,Object> row:pendientes){
				try {
					int res=insert.execute(row);
					if(res>0)
						logger.info("Registro insertado: "+row);
				} catch (Exception e) {
					logger.error("Error insertando registro causa: "+ExceptionUtils.getRootCauseMessage(e));
				}
			}
			importarPartidasFaltantes(fecha, template, sucursalId);
			importarBorrados(fecha, template, sucursalId);
					
		}		
	}
	
	public void importarPartidasFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select APLICACION_ID from SX_CXC_APLICACIONES where date(fecha)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros de Partidas detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(Aplicacion.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Partidas faltantes localizadas: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_CXC_APLICACIONES where APLICACION_ID=?", new Object[]{id});
				pendientes.add(row);
			}
			int[] res=insertPartidas.executeBatch(pendientes.toArray(new Map[0]));
			logger.info(" Partidas Insertados: "+res.length);
		}		
	}
	
	public void importarBorrados(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select ABONO_ID from SX_CXC_ABONOS_BORRADOS where date(CREADO)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros detectados: "+rows.size());
		if(!rows.isEmpty()){
			
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:rows){
				Map<String,Object> row=template.queryForMap("select * from SX_CXC_ABONOS_BORRADOS where ABONO_ID=?", new Object[]{id});
				pendientes.add(row);				
			}
			for(Map<String,Object> row:pendientes){
				try {
					int res=insertBorrados.execute(row);					
					if(res>0)
						logger.info("Registro insertado: "+row);
				} catch (Exception e) {
					logger.error("Error insertando registro causa: "+ExceptionUtils.getRootCauseMessage(e));
				}				
			}
			
			for(Map<String,Object> row:pendientes){
				String id=(String)row.get("ABONO_ID");
				Abono found=(Abono)ServiceLocator2.getHibernateTemplate().get(Abono.class, id);
				if(found!=null)
					ServiceLocator2.getUniversalDao().remove(Abono.class, id);
			}
			
		}		
	}

}
