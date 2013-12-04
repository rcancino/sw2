package com.luxsoft.sw2.replica.consumers;

import java.util.Date;

import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;

/**
 * Importador
 * @author pato
 *
 */
public class ImportadorLocalDeExistencias extends ImportadorLocalExcluyente{
	
	/**
	 * Importa la existencia al ser de otra sucursal. 
	 */
	@Override
	public void doImportar(EntityLog log) {
		Existencia exis=(Existencia)log.getBean();
		Existencia target=ServiceLocator2.getExistenciaDao().buscar(exis.getClave(), exis.getSucursal().getId(), exis.getYear(), exis.getMes());
		if(target!=null){
			if(!target.getId().equals(exis.getId())){
				ServiceLocator2.getUniversalDao().remove(Existencia.class, exis.getId());
				logger.info("Existencia ya registrada con otro id, se ha eliminado para insertar la mas reciente");
			}
		}
		exis.setImportado(new Date());				
		ServiceLocator2.getHibernateTemplate().replicate(exis, ReplicationMode.OVERWRITE);
		logger.info("Entidad importada: "+log);
	}

}
