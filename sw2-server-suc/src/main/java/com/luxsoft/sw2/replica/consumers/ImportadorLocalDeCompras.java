package com.luxsoft.sw2.replica.consumers;

import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorLocalDeCompras implements Importador{
	
	Logger logger=LoggerHelper.getLogger();
	
	
	
	public void importar(EntityLog log) {
		Compra2 compra=(Compra2)log.getBean();
		eliminarPartidasIfNecesary(compra);
		getHibernateTemplate().replicate(compra, ReplicationMode.OVERWRITE);			
		compra.setImportado(new Date());
		ServiceLocator2.getUniversalDao().save(compra);				
		logger.info("Compra replicada: "+compra.getId());
	}
	
	
	/**
	 * Parche para eliminar partidas BUG de la replicacion
	 * 
	 * @param compra
	 * @param target
	 */
	private void eliminarPartidasIfNecesary(final Compra2 compra){		
		try {
			for(CompraUnitaria cu:compra.getPartidas()){
				if(cu.getRecibido()>0)
					continue;
			}
			String UPDATE="delete from CompraUnitaria where compra.id=?";
			int res=getHibernateTemplate().bulkUpdate(UPDATE, compra.getId());
			logger.info("Partidas eliminadas: "+res);
		} catch (Exception e) {
			logger.info("Compra inmutable: "+compra.getId()+ " sucursal: "+compra.getSucursal()+ " Al eliminar partidas: "+ExceptionUtils.getRootCauseMessage(e));
		}
	}

	private HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
}
