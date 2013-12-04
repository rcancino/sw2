package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;


import com.luxsoft.sw3.embarque.ClientePorTonelada;
import com.luxsoft.sw3.services.Services;


/**
 * Replicador de instancias de clientes por tonelada
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeClientesPorTonelada {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	/**
	 * Replica todos los cleients por toneladas pendientes
	 * 
	 */
	public void replicar(){
		String hql="from ClientePorTonelada c where c.replicado is null";
		List<ClientePorTonelada> list=Services.getInstance().getHibernateTemplate().find(hql);
		logger.info("Clientes pendientes por replicar: "+list.size());
		for(Long sucursalId:getSucursales()){
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			for(ClientePorTonelada c:list){
				try {
					c.setReplicado(new Date());
					target.replicate(c, ReplicationMode.IGNORE);
					Services.getInstance().getHibernateTemplate().update(c);
					logger.info("Cliente por tonelada replicado: "+c);
				} catch (Exception e) {
					logger.error("Error replicando depositos a la sucursal: "+sucursalId
							+"  "+ExceptionUtils.getRootCause(e),e);
				}
			}
		}
	}
	
	
	public ReplicadorDeClientesPorTonelada addSucursal(Long... sucursales){
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
		
		ReplicadorDeClientesPorTonelada replicador=new ReplicadorDeClientesPorTonelada();
		replicador.addSucursal(2L,3L,6L,5L)
		.replicar();
		
	}

}
