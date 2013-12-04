package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;


import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeFichas implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	SimpleJdbcInsert insertPartidas;
	
	
	public ImportadorCentralDeFichas(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_FICHAS");
		insertPartidas=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_FICHASDET");
		
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select FICHA_ID from SX_FICHAS where date(creado)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(Ficha.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Faltantes localizados: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_FICHAS where FICHA_ID=?", new Object[]{id});
				
				pendientes.add(row);
			}
			int[] res=insert.executeBatch(pendientes.toArray(new Map[0]));
			logger.info("Insertados: "+res.length);
			importarPartidasFaltantes(fecha, template, sucursalId);
			
					
		}		
	}
	
	public void importarPartidasFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select FICHADET_ID from SX_FICHASDET where FICHA_ID in (select FICHA_ID from SX_FICHAS where date(creado)=?)";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros de Partidas detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(FichaDet.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Partidas faltantes localizadas: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_FICHASDET where FICHADET_ID=?", new Object[]{id});
				pendientes.add(row);
			}
			int[] res=insertPartidas.executeBatch(pendientes.toArray(new Map[0]));
			logger.info(" Partidas Insertados: "+res.length);
		}		
	}
	
	

}
