package com.luxsoft.sw3.services;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.utils.LoggerHelper;

public class SolicitudDeDepositosServices {
	
	static Logger logger=LoggerHelper.getLogger();
	
	public static final String SOLICITUDES_POR_AUTORIZAR_QUEUE="sol.dep.pendientes";
	public static final String SOLICITUDES_AUTORIZADAS_QUEUE="sol.dep.autorizadas";
	
	public static void enviarSolicitud(final SolicitudDeDeposito sol){
		/*
		getCentralBrokerTemplate().convertAndSend(SOLICITUDES_POR_AUTORIZAR_QUEUE, sol, new MessagePostProcessor() {
			public Message postProcessMessage(Message message) throws JMSException {
				message.setJMSCorrelationID(sol.getId());
				message.setJMSReplyTo(new ActiveMQQueue(SOLICITUDES_AUTORIZADAS_QUEUE));
				return message;
			}
		});
		logger.info("Enviando solicitud de deposito para su autorizacón a : "+SOLICITUDES_POR_AUTORIZAR_QUEUE);
		*/
	}
	
	
	public static JmsTemplate getCentralBrokerTemplate(){
		JmsTemplate jmsTemplate=(JmsTemplate)Services.getInstance().getContext().getBean("centralBrokerJmsTemplate");
		return jmsTemplate;
	}	
	

}
