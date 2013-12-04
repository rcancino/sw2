package com.luxsoft.sw2.replica.consumers;

import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeSolicitudDeDepositos implements Importador{
	
	protected Logger logger=LoggerHelper.getLogger();

	public void importar(final EntityLog log) {
		SolicitudDeDeposito sol =(SolicitudDeDeposito)log.getBean();
		if(sol.getPago()==null){
			ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);		
			logger.info("Solicitud importada:  "+log);
		}else{
			logger.info("Solicitud ya autorizada no se puede importar a oficinas  "+log);
		}
				
	}

}
