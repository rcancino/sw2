package com.luxsoft.sw3.replica;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
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

import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeListaDePrecios {
	
private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	public void replicar(boolean limpiar,boolean soloPendientes){
		if(limpiar)
			limpiarListas();
		replicar(soloPendientes);
	}
	
	/**
	 * Replica solo las listas de precios pendientes, sin limpiar las existentes
	 * 
	 */
	public void replicar(){
		replicar(true);
	}
	
	
	
	/**
	 * Replica todas las solicitudes pendientes
	 * 
	 * @param soloPendientes Verdadero si solo se requiere replicar las pendientes replicado == null
	 */
	public void replicar(boolean soloPendientes){
		String hql=" from ListaDePrecios l left join fetch l.precios p where l.vigente = true";
		if(soloPendientes)
			hql+= " and  l.replicado is null ";
		EventList<ListaDePrecios> source=GlazedLists.eventList(Services
		.getInstance()
		.getHibernateTemplate()
		.find(hql));		
		Comparator<ListaDePrecios> c1=GlazedLists.beanPropertyComparator(ListaDePrecios.class, "id");
		UniqueList<ListaDePrecios> listas=new UniqueList<ListaDePrecios>(source,c1);
		if(listas.size()==0){
			return;
		}
		logger.info("Listas a replicar: "+listas.size()+ "  Detalles: "+source.size());
		for(ListaDePrecios lista:listas){
			lista.setReplicado(new Date());
			for(Long sucursalId:getSucursales()){				
				HibernateTemplate target =ReplicaServices.getInstance().getHibernateTemplate(sucursalId);				
				try {
					eliminarExistente(lista.getId(), target);
					target.replicate(lista, ReplicationMode.OVERWRITE);	
					logger.info("Lista replicada a la sucursal: "+sucursalId);
				} catch (Exception e) {
					logger.error("Error replicando lista : "+lista.getId()+ " a la sucursal: "+sucursalId
							+"  "+ExceptionUtils.getRootCauseMessage(e),e);	
				}					
			}
			//Services.getInstance().getHibernateTemplate().update(lista);
			try {
				Services.getInstance()
				.getJdbcTemplate()
				.update("UPDATE SX_LP_PROVS SET TX_REPLICADO=NOW() WHERE ID=?"
						,new Object[]{lista.getId()}
						);
			} catch (Exception e) {
				logger.info("No se pudo actualizar el campo de replicado en la lista: "+lista.getId()+ " "+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	
	
	/**
	 * 
	 * @param id
	 * @param target
	 */
	private void eliminarExistente(Long id, HibernateTemplate target){
		//HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		try {			
			String DELETE="delete ListaDePreciosDet lp where lp.lista.id=?";
			target.bulkUpdate(DELETE,id);
		} catch (DataAccessException e) {			
			logger.error(e);
		}
	}
	
	/**
	 * Limpia las listas de precios existentes
	 * 
	 * @return
	 */
	public ReplicadorDeListaDePrecios limpiarListas(){
		for(Long sucursalId:getSucursales()){
			logger.info("Eliminando listas de precios de proveedores en sucursal: "+sucursalId);
			try {
				JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
				int deleted=template.update("DELETE FROM SX_LP_PROVS_DET");
				deleted=template.update("DELETE FROM SX_LP_PROVS");
				logger.info("Listas eliminadas: "+deleted);
			} catch (Exception e) {
				logger.error(e);
			}
			
		}
		return this;
	}
	
	public ReplicadorDeListaDePrecios addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
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
		
		new ReplicadorDeListaDePrecios()		
		.addSucursales(2L,3L,6L,5L,9L)
		//.limpiarListas()
		.replicar(true,false)
		//.replicar(true)
		;
		
		
	}

}
