package com.luxsoft.sw2.replica.consumers;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeVentas implements Importador{
	
	protected Logger logger=LoggerHelper.getLogger();

	public void importar(final EntityLog log) {
		logger.info("Importando venta: "+log);
		Venta venta=(Venta)log.getBean();
		venta.setPedido(null);
		String comentario=venta.getComentario2();
		boolean cancelada=StringUtils.containsIgnoreCase(comentario, "CANCELAD");
		if(venta.getTotal().doubleValue()==0 || cancelada){
			List<String> abonosIds=ServiceLocator2.getHibernateTemplate().find("select distinct(a.abono.id) from Aplicacion a where a.cargo.id=?", venta.getId());
			if(!abonosIds.isEmpty()){
				logger.info("Eliminando abonos aplicados a factura cancelada");
				for(String id:abonosIds){
					ServiceLocator2.getUniversalDao().remove(Abono.class, id);
					logger.info("Abono eliminado: "+id);
				}
			}				
			ServiceLocator2.getHibernateTemplate().bulkUpdate("delete from VentaDet v where v.venta.id=?", venta.getId());
			ServiceLocator2.getHibernateTemplate().saveOrUpdate(venta);
			logger.info("Factura cancelada actualizada: "+venta.getId());
		}else{
			//ServiceLocator2.getHibernateTemplate()
			Cliente cliente=(Cliente)ServiceLocator2.getHibernateTemplate().get(Cliente.class, venta.getCliente().getId());
			if(cliente==null){
				ServiceLocator2.getHibernateTemplate().replicate(venta.getCliente(), ReplicationMode.IGNORE);
			}
			ServiceLocator2.getHibernateTemplate().replicate(log.getBean(), ReplicationMode.OVERWRITE);		
			logger.info("Venta  importada en CentralBroker  "+log);
		}
				
	}

}
