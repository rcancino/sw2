package com.luxsoft.sw2.replica.consumers;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

/**
 * Importador que exluye la importacion para un grupo de sucursales
 * 
 * @author Ruben Cancino
 *
 */
public class ImportadorLocalExcluyente implements Importador{
	
	Logger logger=LoggerHelper.getLogger();
	
	private  String[] sucursales;


	public void importar(EntityLog log) {
		
		if(excluir(log)){
			logger.info("Modificacion de esta entidad igonrada "+log);
			return;
		}else{
			try {
				doImportar(log);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	protected boolean excluir(EntityLog log){
		return ArrayUtils.contains(getSucursales(), log.getSucursalOrigen());
	}
	
	public void doImportar(EntityLog log){
		ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);
		logger.info("Entidad importada: "+log);
	}


	public String[] getSucursales() {
		return sucursales;
	}


	public void setSucursales(String[] sucursales) {
		this.sucursales = sucursales;
	}
	
	

}
