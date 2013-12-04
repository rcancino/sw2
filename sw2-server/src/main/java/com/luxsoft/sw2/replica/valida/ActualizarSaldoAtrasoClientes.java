package com.luxsoft.sw2.replica.valida;

import java.util.List;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ActualizarSaldoAtrasoClientes {
	
	Logger logger=LoggerHelper.getLogger();
	
	public void actualizar(){
		List<Cliente> clientes=ServiceLocator2.getClienteManager().buscarClientesCredito();
		for(Cliente c:clientes){
			String sql=SQLUtils.loadSQLQueryFromResource("sql/Clientes_credito_atraso_max.sql");
			List<Integer> res=ServiceLocator2.getJdbcTemplate().queryForList(sql,new Object[]{c.getClave()},Integer.class);
			int atraso=0;
			if(!res.isEmpty()){
				atraso=res.get(0);
				if(atraso<0)
					atraso=0;
			}
			c.getCredito().setAtrasoMaximo(atraso);
			//c.getCredito().setSaldo(ServiceLocator2.getClienteServices().getSaldo(c));
			ServiceLocator2.getClienteManager().save(c);
			
			Cliente target=ServiceLocator2.getClienteManager().buscarPorClave(c.getClave());
			logger.info("Cliente: "+c.getClave()+"  Saldo: "+target.getCredito().getSaldo()+"  Atraso maximo: "+atraso+" Modificado: "+c.getUserLog().getModificado());
			EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			ServiceLocator2.getReplicaMessageCreator().enviar(log);
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		new ActualizarSaldoAtrasoClientes().actualizar();
	}

}
