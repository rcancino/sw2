package com.luxsoft.sw2.replica.consumers;

import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorLocalDeSolicitudesDeTraslados implements Importador{
	
	Logger logger=LoggerHelper.getLogger();
	
	private Long sucursalOrigenId;
	
	public void importar(EntityLog log) {
		SolicitudDeTraslado sol=(SolicitudDeTraslado)log.getBean();
		if(sol.getSucursal().getId().equals(getSucursalOrigenId())){
			ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);
			logger.info("Solicitud actualizada importando : "+sol.getDocumento());
		}else if(sol.getOrigen().getId().equals(getSucursalOrigenId())){
			ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);
			logger.info("Solicitud importada para su atencion en la sucursal: "+sol);
			return;
		}
		logger.info("Sol ignorada por ser requerida en otra sucursal "+log);
		
	}

	public Long getSucursalOrigenId() {
		return sucursalOrigenId;
	}

	public void setSucursalOrigenId(Long sucursalOrigenId) {
		this.sucursalOrigenId = sucursalOrigenId;
	}

	
}
