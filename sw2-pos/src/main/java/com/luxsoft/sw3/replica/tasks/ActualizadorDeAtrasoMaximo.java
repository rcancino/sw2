package com.luxsoft.sw3.replica.tasks;

import java.util.List;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.utils.LoggerHelper;

public class ActualizadorDeAtrasoMaximo {
	
	Logger logger=LoggerHelper.getLogger();
	
	public void execute(){
		List<Cliente> clientes=Services.getInstance().getClientesManager().buscarClientesCredito();
		for(Cliente c:clientes){
			String sql=SQLUtils.loadSQLQueryFromResource("sql/Clientes_credito_atraso_max.sql");
			List<Integer> res=Services.getInstance().getJdbcTemplate().queryForList(sql,new Object[]{c.getClave()},Integer.class);
			int atraso=0;
			if(!res.isEmpty()){
				atraso=res.get(0);
				if(atraso<0)
					atraso=0;
			}
			if(c.getCredito().getAtrasoMaximo()!=atraso){
				c.getCredito().setAtrasoMaximo(atraso);
				//c.getComentarios().put("", value)
				Services.getInstance().getClientesManager().save(c);
				logger.info("Atraso maximo actualizado: "+c.getClave()+ " atraso:: "+atraso+" MODIFICADO: "+c.getUserLog().getModificado());
			}
		}
	}
	
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		new ActualizadorDeAtrasoMaximo().execute();
	}

}
