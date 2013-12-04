package com.luxsoft.sw2.replica.consumers;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.beans.factory.InitializingBean;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

/**
 * Manager para la importación en la sucursal de las replicas puestas en REPLICA.TOPIC
 *  
 * 
 * @author Ruben Cancino
 *
 */
public class ImportadorLocalManager implements InitializingBean{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	private Map<Class, Importador> importadores=new HashMap<Class, Importador>();

	public void importarEntidad(EntityLog log){
		System.out.println("Procesando: "+log);
		if(logger.isDebugEnabled()){
			logger.debug("Procesando entidad: "+log);
		}
		
		try {
			Importador importador=importadores.get(log.getBean().getClass());
			if(importador!=null){
				importador.importar(log);
			}else{
				doImportar(log);
			}
			
		} catch (Exception e) {
			String message=MessageFormat.format("Error replicando entidad {0}  Error:{1}", log,ExceptionUtils.getRootCauseMessage(e));
			logger.error(message);
			throw new RuntimeException("Error replicando entidad: "+message,e);
		}	
	}
	
	/**
	 * Importacion estandar de entidades
	 * 
	 * @param log
	 */
	protected void doImportar(final EntityLog log){
		ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);			
		logger.info("Entidad exitosamente importada : "+log);
		
	}

	public void afterPropertiesSet() throws Exception {
		logger.info("Importador manager registrado  atendiendo mensajes de replica en el broker central REPLICA.TOPIC");
		
	}

	public Map<Class, Importador> getImportadores() {
		return importadores;
	}

	public void setImportadores(Map<Class, Importador> importadores) {
		this.importadores = importadores;
	}
	
	

}
