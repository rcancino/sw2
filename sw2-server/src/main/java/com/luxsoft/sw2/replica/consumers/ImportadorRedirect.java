package com.luxsoft.sw2.replica.consumers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.jms.core.MessagePostProcessor;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorRedirect implements Importador{
	
	protected Logger logger=LoggerHelper.getLogger();

	public void importar(final EntityLog log) {
		ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);
		redirect(log);
		logger.info("Mensaje importado y re enviado para ser atendido por sucursales en CentralBroker  "+log);		
	}
	
	public void redirect(final EntityLog log){
		Topic topic=new ActiveMQTopic("REPLICA.TOPIC");
		ServiceLocator2.getJmsTemplate().convertAndSend(topic, log,new MessagePostProcessor() {
			public Message postProcessMessage(Message message) throws JMSException {
				message.setStringProperty("SUCURSAL_ORIGEN", log.getSucursalOrigen());
				return message;
			}
		});
	}

}
