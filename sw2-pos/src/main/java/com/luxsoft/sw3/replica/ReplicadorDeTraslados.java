package com.luxsoft.sw3.replica;

import java.sql.BatchUpdateException;
import java.util.Collection;
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

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador de Traslados de material
 * 
 * 	Procedimiento
 * 		
 * 		1 .  Importar todas las solicitudes pendientes a produccion
 * 				(from SolicitudDeTraslado sol where sol.replicado is null)
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeTraslados {
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	private Logger logger=Logger.getLogger(getClass());
	
	public HibernateTemplate getProduccionTemplate(){
		return Services.getInstance().getHibernateTemplate();
	}
	
	
	/**
	 * Importa solicitudes pendientes. Asume q la solicitud no existe y manda error se esta ya existe
	 * Actualiza el campo de SolicitudDeTraslado.replicado en la sucursal local de la solicitud
	 * despues de ser replicado por lo que el mismo campo queda nulo en produccion
	 * @param sucursalDestino
	 * @param source
	 * @param target
	 */
	public void importarSolicitudesPendientes(Long sucursalOrigen){
		
		
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalOrigen);		
		
		
		String hql="from SolicitudDeTraslado s where s.sucursal.id=? and s.replicado is null";
		List<SolicitudDeTraslado> sols=source.find(hql,sucursalOrigen);
		if(!sols.isEmpty()){
			logger.info("Importando "+sols.size()+"  solicitudes pendientes de la sucursal: "+sucursalOrigen);
		}
		
		for(SolicitudDeTraslado sol:sols){
			try {
				getProduccionTemplate().replicate(sol, ReplicationMode.EXCEPTION);
				sol.setReplicado(new Date());
				source.update(sol);
				logger.info("Solicitud importada en produccion: "+sol.getId());
			} catch (Exception e) {
				//e.printStackTrace();
				String msg=ExceptionUtils.getRootCauseMessage(e);
				logger.error("Error imprtando sol: "+sol + "  Id:"+sol.getId()+ " Causa: "+msg);
			}
		}
	}
	
	/**
	 * Replicamos las solicitudes pendientes desde Produccion hasta la sucursal destino
	 * 
	 * @param sucursalDestino
	 */
	public void enviarSolicitudesPendientes(Long sucursalDestino){
		String hql="from SolicitudDeTraslado s where s.origen.id=? and s.replicado is null";
		List<SolicitudDeTraslado> sols=getProduccionTemplate().find(hql,sucursalDestino);
		
		if(!sols.isEmpty()){			
			logger.info("Exportando "+sols.size()+"  solicitudes pendientes a la sucursal: "+sucursalDestino);
		}
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalDestino);
		for(SolicitudDeTraslado sol:sols){
			try {
				target.replicate(sol, ReplicationMode.IGNORE);
				logger.info("Solicitud enviada a a la sucursal: "+sucursalDestino+ " "+sol.getId());
				sol.setReplicado(new Date());
				getProduccionTemplate().update(sol);
				logger.info("Solicitud actualizada: "+sol.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Importa los traslados atentidos a la base de datos central (PRODUCCION)
	 * 
	 * Utiliza el campo de Traslado.replicado para saber si la solicitud ya ha sido
	 * importada, este mismo campo se actualiza solo y solo en la sucursal origen
	 * en produccion se deja nulo y se actualiza hasta que el traslado es enviado
	 * a su sucursal destino
	 * 
	 * @param sucursalOrigen
	 */
	public void importarTrasladosPendientes(Long sucursalOrigen){
		
		
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalOrigen);
		
		String hql="from Traslado t left join fetch t.partidas where t.solicitud.origen.id=? " 
				+"and t.replicado is null"
			;		
		List<Traslado> data=source.find(hql, sucursalOrigen);
		EventList<Traslado> eventList=GlazedLists.eventList(data);
		UniqueList<Traslado> traslados=new UniqueList<Traslado>(eventList,GlazedLists.beanPropertyComparator(Traslado.class,"id"));
		if(traslados.size()>0)
			logger.info("Importando "+traslados.size()+"  traslados pendientes de la sucursal: "+sucursalOrigen);
		for(Traslado t:traslados){
			try {
				getProduccionTemplate().replicate(t, ReplicationMode.OVERWRITE);
				t.setReplicado(new Date());
				source.update(t);
				logger.info("Traslado importado a produccion: "+t.getId());
			} catch (Exception e) {				
				if(ExceptionUtils.getRootCause(e) instanceof BatchUpdateException){
					BatchUpdateException be=(BatchUpdateException)ExceptionUtils.getRootCause(e);
					if(be.getErrorCode()==1452){
						logger.info("Posiblemente sol no importado");
						getProduccionTemplate().replicate(buscarSolicitud(source, t.getSolicitud().getId()), ReplicationMode.LATEST_VERSION);
						getProduccionTemplate().replicate(t, ReplicationMode.OVERWRITE);
						t.setReplicado(new Date());
						source.update(t);
						logger.info("Traslado importado a produccion: "+t.getId());
					}
				}else{
					logger.error("Error importando traslado pendiente: "+t.getId()+ "  "+ExceptionUtils.getRootCauseMessage(e),e);
				}
			}
		}
			
	}
	
	/**
	 * Replica los traslados generados y pendientes a la sucursal origen de
	 * la solicitud
	 * NOTA: La Actualizacion de las existencias en la sucursal destino de la
	 * entrada es mediante un TRIGGER
	 * 
	 * @param sucursalDestino
	 */
	public void enviarTrasladosPendientes(Long sucursalDestino){
		
		String hql="from Traslado t left join fetch t.partidas where t.solicitud.sucursal.id=? " 
				+"and t.replicado is null"
			;
		List<Traslado> data=getProduccionTemplate().find(hql,sucursalDestino);
		EventList<Traslado> eventList=GlazedLists.eventList(data);
		UniqueList<Traslado> traslados=new UniqueList<Traslado>(eventList,GlazedLists.beanPropertyComparator(Traslado.class,"id"));
		if(!traslados.isEmpty()){
			logger.info("Exportando "+traslados.size()+"  traslados pendientes a la sucursal: "+sucursalDestino);
		}
		
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalDestino);
		for(Traslado t:traslados){
			logger.info("Traslado: " +t.getId());
			try {
				target.replicate(t, ReplicationMode.OVERWRITE);
				logger.info("Traslado enviada a a la sucursal: "+sucursalDestino+ " "+t.getId());
				t.setReplicado(new Date());
				getProduccionTemplate().update(t);
				logger.info("Traslado actualizado en produccion: "+t.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private SolicitudDeTraslado buscarSolicitud(final HibernateTemplate source,final String id){
		String hql="from SolicitudDeTraslado s where  s.id=?";
		List<SolicitudDeTraslado> sols=source.find(hql,id);
		return sols.isEmpty()?null:sols.get(0);
	}
	
	public void replicarPendientes(){
		//Long[] sucursales={3L,5L,6L,2L,7L};
		//Long[] sucursales={6L};
		for(Long sucursalId:getSucursales()){
			importarSolicitudesPendientes(sucursalId);
			enviarSolicitudesPendientes(sucursalId);
			importarTrasladosPendientes(sucursalId);
			enviarTrasladosPendientes(sucursalId);
		}
	}
	
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeTraslados addSucursal(Long... sucursales){
		for (Long sucursalId:sucursales){
			getSucursales().add(sucursalId);
		}
		return this;
	}

	public static void main(String[] args) {
		ReplicadorDeTraslados replicador=new ReplicadorDeTraslados();
		replicador.addSucursal(2L,3l,5l,6l,9l)
		.replicarPendientes();
		//replicador.enviarTrasladosPendientes(9L);
	}
	

}
