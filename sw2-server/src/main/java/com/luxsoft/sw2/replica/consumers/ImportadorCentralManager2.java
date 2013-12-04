package com.luxsoft.sw2.replica.consumers;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.beans.factory.InitializingBean;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.replica.Replicable;
import com.luxsoft.utils.LoggerHelper;

/**
 * Mnager para la importacion de mensajes de replica generados en las sucursales
 * Registrado a un MessageListenerContaine de spring y atenidendo la cola REPLICA.QUEUE
 *  
 * 
 * @author Ruben Cancino
 *
 */
public class ImportadorCentralManager2 implements InitializingBean{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	protected ReplicationMode replicationMode=ReplicationMode.OVERWRITE;
	
	private Map<Class, Importador> importadores=new HashMap<Class, Importador>();
	
	
	public void init(){
		
	}
	
	/**
	 * Importa entidades delegando en su caso a un importador registrado para la clase del bean
	 *  a importar
	 *  
	 * @param log
	 */
	public void importarEntidad(EntityLog log){
		logger.debug("Atendiendo: "+log);
		try {
			beforeImport(log);
			//Verificamos si existe un importador especial para la clase de la entidad
			Importador importer=importadores.get(log.getBean().getClass());
			if(importer!=null){
				importer.importar(log);
			}else{
				doImportar(log);
			}
			
		} catch (Exception e) {
			String message=MessageFormat.format("Error replicando entidad {0}  Error:{1}", log,ExceptionUtils.getRootCauseMessage(e));
			logger.error(message);
			throw new RuntimeException("Error replicando entidad: "+message,e);
		}	
	}
	
	public void beforeImport(EntityLog log){
		Object bean=log.getBean();
		if(bean instanceof Replicable){
			Replicable r=(Replicable)bean;
			r.setImportado(new Date());
		}
	}
	
	/**
	 * Importacion estandar de entidades 
	 * 
	 * 
	 * @param log
	 */
	protected void doImportar(final EntityLog log){
		ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), this.replicationMode);			
		logger.info("Entidad exitosamente importada y replicada: "+log);
	}

	public void afterPropertiesSet() throws Exception {		
		logger.info("Replica Topic Dispatcher atendiendo mensajes de replica en las sucursales en la cola: REPLICA.QUEUE");
		logger.info("Importadores registrados: "+importadores.size());
		
	}
	
	
	public void setReplicationMode(ReplicationMode replicationMode) {
		this.replicationMode = replicationMode;
	}

	public void setImportadores(Map<Class, Importador> importadores) {
		this.importadores = importadores;
	}

	
	
	
	

}
