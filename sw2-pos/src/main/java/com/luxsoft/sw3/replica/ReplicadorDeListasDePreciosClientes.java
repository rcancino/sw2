package com.luxsoft.sw3.replica;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador de Listas de precios de clientes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeListasDePreciosClientes {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	
	/**
	 * Replica todas las solicitudes pendientes
	 * 
	 */
	public void replicar(){
		//logger.info("Replicando lista de precios de cliente");
		String hql=" from ListaDePreciosCliente l left join fetch l.precios p where l.replicado is null";
		
		EventList<ListaDePreciosCliente> source=GlazedLists.eventList(Services
		.getInstance()
		.getHibernateTemplate()
		.find(hql));		
		Comparator<ListaDePreciosCliente> c1=GlazedLists.beanPropertyComparator(ListaDePreciosCliente.class, "id");
		UniqueList<ListaDePreciosCliente> listas=new UniqueList<ListaDePreciosCliente>(source,c1);
		
		if(listas.size()==0){
			return;
		}
		logger.info("Listas de clientes a replicar: "+listas.size()+ "  Detalles: "+source.size());
		for(ListaDePreciosCliente lista:listas){
			lista.setReplicado(new Date());
			for(Long sucursalId:getSucursales()){				
				HibernateTemplate target =ReplicaServices.getInstance().getHibernateTemplate(sucursalId);				
				try {
					eliminarExistente(lista.getId(), target);
					target.replicate(lista, ReplicationMode.OVERWRITE);	
					logger.info("Lista replicada a la sucursal: "+sucursalId+  " Lista: "+lista.getId());
				} catch (Exception e) {
					logger.error("Error replicando lista de precios de cliente: "
							+lista.getCliente().getClave()+ " Id: "+lista.getId()+ " a la sucursal: "+sucursalId
							+"  "+ExceptionUtils.getRootCause(e),e);	
				}					
			}
			Services.getInstance().getHibernateTemplate().update(lista);
		}
	}
	
	private void eliminarExistente(Long id, HibernateTemplate target){
		try {
			
			String DELETE_1="delete ListaDePreciosClienteDet lp where lp.lista.id=?";
			String DELETE_2="delete ListaDePreciosCliente lp where lp.id=?";
			target.bulkUpdate(DELETE_1,id);
			target.bulkUpdate(DELETE_2,id);
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.error(e);
		}
		/*
		List<ListaDePreciosCliente> found=target.find(
				" from ListaDePreciosCliente l " +
				" left join fetch l.precios p " +
				" where l.id=? ",id);
		if(!found.isEmpty()){
			target.delete(found.get(0));
			logger.info("  Lista existente  eliminada");
		}*/
	}
	
	
	
	private void clean(){
		for(Long sucursalId:getSucursales()){
			logger.info("Eliminando listas de precios por cliente en sucursal: "+sucursalId);
			try {
				JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
				int deleted=template.update("DELETE FROM SX_LP_CLIENTE_DET");
				deleted=template.update("DELETE FROM SX_LP_CLIENTE");
				logger.info("Listas eliminadas: "+deleted);
			} catch (Exception e) {
				logger.error(e);
			}
			
		}
	}
	
	public ReplicadorDeListasDePreciosClientes addSucursal(Long... sucursales){
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
		
		ReplicadorDeListasDePreciosClientes replicador=new ReplicadorDeListasDePreciosClientes();
		replicador.addSucursal(3L,2L,5L,6L,9L)		
		//.replicar(false, false);
		.replicar();
		
	}

}
