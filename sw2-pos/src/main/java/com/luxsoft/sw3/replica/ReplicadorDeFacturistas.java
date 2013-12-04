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

import com.luxsoft.sw3.embarque.ChoferFacturista;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeFacturistas {
	
private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Replica los facturistas pendientes  de replica para todas las
	 * sucursales
	 * 
	 */
	public void replicar(){
		
		String hql="from ChoferFacturista c " +
			" left join fetch c.choferes" +
			" where c.replicado is null";
		EventList<ChoferFacturista> data=GlazedLists.eventList(Services.getInstance().getHibernateTemplate().find(hql));
		UniqueList<ChoferFacturista> choferes=new UniqueList<ChoferFacturista>(data,GlazedLists.beanPropertyComparator(ChoferFacturista.class, "id"));
		for(ChoferFacturista c:choferes){
			c.setReplicado(new Date());
			for(Long sucursalId:getSucursales()){
				try {
					HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
					target.replicate(c, ReplicationMode.OVERWRITE);
				} catch (Exception e) {				
					logger.error(
							"Error replicando facturistas para la sucursal: "+sucursalId
							+"  "+ExceptionUtils.getRootCause(e),e);				
				}	
			}
			
			Services.getInstance().getHibernateTemplate().update(c);
			logger.info("FacturistaChofer replicado: "+c);
		}		
	}
	
	/**
	 * Permite replicar un solo ChoferFacturista a una sucursal indicada
	 * 
	 * @param id
	 * @param sucursalId
	 */
	public ReplicadorDeFacturistas replicar(Long id,Long sucursalId){
		String hql="from ChoferFacturista c " +
		" left join fetch c.choferes" +
		" where c.id=?";
		List<ChoferFacturista> data=Services.getInstance().getHibernateTemplate().find(hql,id);
		if(!data.isEmpty()){
			ChoferFacturista c=data.get(0);
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			target.replicate(c, ReplicationMode.OVERWRITE);
		}
		return this;
	}
	
	public ReplicadorDeFacturistas addSucursal(Long... sucursales){
		for (Long sucursalId:sucursales){
			getSucursales().add(sucursalId);
		}
		return this;
	}
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public static void main(String[] args) {
		new ReplicadorDeFacturistas()
		.addSucursal(2L,3L,5L,6L)
		.replicar();		
		//.replicar(22L,2L)
		//.replicar(22L,5L);
		//.replicar(19L,6L);
	}

}
