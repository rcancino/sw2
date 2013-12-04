package com.luxsoft.sw2.replica.valida;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;



import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ReplicadorCentralDeCompras implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		Class clazz=Compra2.class;
		
		String sql="select COMPRA_ID from SX_COMPRAS2 where date(modificado)=?";
		List<String> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		
		logger.info("Registros detectados: "+rows.size());
		if(!rows.isEmpty()){
			for(String row:rows){
				String id=row;
				Compra2 target=ServiceLocator2.getComprasManager().buscarInicializada(id);
				EntityLog log=new EntityLog(target,target.getId(),target.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
				if(target.getProveedor().getId().equals(60L) && target.getSucursal().getId().equals(1l)){
					String destino="REPLICA.TOPIC";
					Topic topic=new ActiveMQTopic(destino);
					ServiceLocator2.getJmsTemplate().convertAndSend(topic, log);
					logger.info("JMS enviado de replica Compra Importaciones: "+id+ " Al destino: "+topic);
				}else{
					
					String destino="REPLICA.QUEUE."+target.getSucursal().getNombre();
					Queue queue=new ActiveMQQueue(destino);
					ServiceLocator2.getJmsTemplate().convertAndSend(queue, log);
					logger.info("JMS enviado de replica Compra: "+id+ " Al destino: "+queue);
				}
			}
		
		}		
	}
	
	
	
}
