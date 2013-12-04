package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeClientes implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	
	public ImportadorCentralDeClientes(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_CLIENTES");
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select CLIENTE_ID from SX_CLIENTES where DATE(CREADO)=?";
		List<Long> clientes=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),Long.class);
		if(!clientes.isEmpty()){
			List<Long> faltantes=new ArrayList<Long>();
			for(Long id:clientes){
				Object found=ServiceLocator2.getHibernateTemplate().get(Cliente.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Faltantes localizados: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(Long id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from sx_clientes where cliente_id=?", new Object[]{id});
				pendientes.add(row);
			}
			int[] res=insert.executeBatch(pendientes.toArray(new Map[0]));
			logger.info("Insertados: "+res.length);
		}		
	}

}
