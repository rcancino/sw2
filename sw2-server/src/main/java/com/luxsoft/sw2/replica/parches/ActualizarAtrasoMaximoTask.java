package com.luxsoft.sw2.replica.parches;

import java.util.List;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.sw3.replica.EntityLog;

import com.luxsoft.utils.LoggerHelper;

public class ActualizarAtrasoMaximoTask {
	
	Logger logger=LoggerHelper.getLogger();
	
	public void execute(){
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
			if(c.getCredito().getAtrasoMaximo()!=atraso){
				try {					
					c.getCredito().setAtrasoMaximo(atraso);
					ServiceLocator2.getClienteManager().save(c);
					c=ServiceLocator2.getClienteManager().get(c.getId());
					logger.info("Atraso maximo actualizado: "+c.getClave()+ " atraso: "+atraso+" MODIFICADO: "+c.getUserLog().getModificado());
					
					EntityLog log=new EntityLog(c,c.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
					String destino="REPLICA.TOPIC";
					Topic topic=new ActiveMQTopic(destino);
					ServiceLocator2.getJmsTemplate().convertAndSend(topic, log);
					logger.info("JMS enviado de replica Cliente: "+c.getId()+ " Al destino: "+topic);
					
				} catch (Exception e) {
					logger.error("Imposible replicar cliente: "+c+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		new ActualizarAtrasoMaximoTask().execute();
	}

}
