package com.luxsoft.sw2.replica.consumers;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorLocalDeSolicitudDePagoConDeposito  implements Importador{
	
	Logger logger=LoggerHelper.getLogger();
	
	public void importar(EntityLog log) {
		SolicitudDeDeposito sol=(SolicitudDeDeposito)log.getBean();
		logger.info("Importando solicitud: "+sol);		
		if(sol.getPago()!=null){			
			PagoConDeposito deposito=sol.getPago();
			if(deposito.getLog()!=null){
				Date creado=deposito.getLog().getCreado();
				deposito.setSafeFecha(creado);
				deposito.setLiberado(creado);
			}			
		}		
		ServiceLocator2.getHibernateTemplate().replicate(sol, ReplicationMode.OVERWRITE);		
	}


}
