package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador para importar devoluciones de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeDevoluciones2 {
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	private Logger logger=Logger.getLogger(getClass());
	
	public void importar(){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId);
		}
		
	}
	
	public void importar(Long sucursalId){
		
		String hql="from Devolucion m left join fetch m.partidas p where m.importado=null and m.fecha>=?";
		final HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		final HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<Devolucion> data=source.find(hql, DateUtil.toDate("01/01/2011"));
		EventList<Devolucion> eventList=GlazedLists.eventList(data);
		UniqueList<Devolucion> devs=new UniqueList<Devolucion>(eventList,GlazedLists.beanPropertyComparator(Devolucion.class,"id"));
		if(devs.size()>0){
			logger.info("Importando devoluciones de ventas pendientes de la sucursal: "+sucursalId+ " Devs:"+devs.size());
		}
		for(Devolucion d:devs){
			try {
				d.setImportado(new Date());
				target.replicate(d, ReplicationMode.OVERWRITE);
				source.update(d);
				logger.info("Devolucion importada: "+d.getId());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(ExceptionUtils.getRootCause(e),e);
			}
		}
	}
	
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeDevoluciones2 addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
		}
		return this;
	}
	
	public static void main(String[] args) {
		ReplicadorDeDevoluciones2 replicador=new ReplicadorDeDevoluciones2();
		replicador.addSucursales(3L);
		replicador.importar();
		
	}

}
