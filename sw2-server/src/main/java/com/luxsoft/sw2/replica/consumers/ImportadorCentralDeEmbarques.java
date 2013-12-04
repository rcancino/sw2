package com.luxsoft.sw2.replica.consumers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.replica.EntityLog;

import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeEmbarques implements Importador{
	
	protected Logger logger=LoggerHelper.getLogger();

	public void importar(final EntityLog log) {
		Embarque embarque =(Embarque)log.getBean();
		
		switch (log.getTipo()) {
		case ALTA:
			ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);
			logger.debug("Embarque importado: "+log);
			break;
		case CAMBIO:
			List<String> embarquesIds=ServiceLocator2.getHibernateTemplate().find("select distinct(a.id) from Embarque a where a.id=?", embarque.getId());
			if(!embarquesIds.isEmpty()){
				ServiceLocator2.getUniversalDao().remove(Embarque.class, embarque.getId());	
			}
		/*	String hql="select distinct(a.embarque.id) from Embarque a where a.embarque.id=?";
			List<String> embarquesIds=ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{embarque.getId()});
			if(!embarquesIds.isEmpty()){
			ServiceLocator2.getUniversalDao().remove(Embarque.class, embarque.getId());	
		}*/
			
			//ServiceLocator2.getUniversalDao().remove(Embarque.class, embarque.getId());	
			ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);
			logger.debug("Embarque actualizado: "+log);
			break;
		case BAJA:
			ServiceLocator2.getUniversalDao().remove(Embarque.class, embarque.getId());
			logger.debug("Embarque eliminado: "+log);
		default:
			break;
		}
		
				
	}
	
	public static void main(String[] args) {
		String id="8a8a81ee-379e1362-0137-9f2415b4-003a";
		List<String> embarquesIds=ServiceLocator2.getHibernateTemplate().find("select distinct(a.id) from Embarque a where a.id=?",id);
		if(!embarquesIds.isEmpty()){
			ServiceLocator2.getUniversalDao().remove(Embarque.class,id);	
		}
	}

}
