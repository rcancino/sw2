package com.luxsoft.sw2.replica.consumers;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorLocalDeTraslados  implements Importador{
	
	Logger logger=LoggerHelper.getLogger();
	
	private Long sucursalOrigenId;
	
	private JmsTemplate jmsTemplate;
	
	public void importar(EntityLog log) {
		
		Traslado t=(Traslado)log.getBean();
		
		if(t.getSolicitud().getSucursal().getId().equals(getSucursalOrigenId())){
			ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);
			logger.info("Traslado importando : "+t);
			if(t.getTipo().equals("TPE")){
				try {
					for(TrasladoDet det:t.getPartidas()){
						ServiceLocator2.getExistenciaDao().actualizarExistencias(det.getSucursal().getId(), det.getClave(), det.getYear(), det.getMes());
						Existencia target=ServiceLocator2.getExistenciaDao().buscar(det.getClave(), det.getSucursal().getId(),det.getYear(), det.getMes());
						
						EntityLog entity=new EntityLog(target,target.getId(),target.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
						jmsTemplate.convertAndSend("REPLICA.QUEUE",entity);
						logger.info(" Mensaje JMS de Existencia actualizada enviada: "+entity
								+ " Destino: REPLICA.QUEUE");		
					}
					
				} catch (Exception e) {
					logger.info(ExceptionUtils.getRootCauseMessage(e));
				}
			}
			
		}
		
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	public Long getSucursalOrigenId() {
		return sucursalOrigenId;
	}

	public void setSucursalOrigenId(Long sucursalOrigenId) {
		this.sucursalOrigenId = sucursalOrigenId;
	}


}
