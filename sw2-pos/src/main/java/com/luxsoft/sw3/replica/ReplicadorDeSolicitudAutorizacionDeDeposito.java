package com.luxsoft.sw3.replica;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;


import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Replicador de solicitudes de pago
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeSolicitudAutorizacionDeDeposito {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	public void refresh(){
		importar(new Date());
		replicar();
	}
	
	/**
	 * Importa todas las solicitudes pendientes
	 * 
	 */
	public void importar(Date fecha){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId,fecha);
		}
	}
	
	/**
	 * Importa todas las solicitudes  <b>PENDIENTES</b> de una sucursal a la base de datos central
	 *  
	 * 
	 * @param fecha
	 * @param sucursalId
	 * @param origen
	 * @param sourceTemplate
	 * @param targetTemplate
	 */
	public void importar(Long sucursalId,final Date fecha){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> pendientes=source
					.find("select s.id from SolicitudDeDeposito s where date(s.fecha)=? and s.importado is null and s.pago is null "
							,new Object[]{fecha});
		for(String id:pendientes){
			importar(id, sucursalId);
		}
	}
	
	/**
	 * Importa todas las solicitudes pendientes
	 * 
	 */
	public void importarForzada(String fecha){
		for(Long sucursalId:getSucursales()){
			importarForzada(sucursalId,fecha);
		}
	}
	
	/**
	 * 
	 * @param sucursalId
	 * @param fecha en formato dd/MM/yyyy
	 */
	public void importarForzada(Long sucursalId,String fecha){
		importarForzada(sucursalId,DateUtil.toDate(fecha))
;	}
	
	public void importarForzada(final Long sucursalId,Date fecha){
		final HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		final HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<String> pendientes=source
					.find("select s.id from SolicitudDeDeposito s where " +
							" s.importado is null " 
							+ "and date(s.fecha)=?"
							,fecha);
		for(final String id:pendientes){
			//String hql="from SolicitudDeDeposito s where s.id=?";
			
			source.execute(new HibernateCallback(){
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					SolicitudDeDeposito solicitud=(SolicitudDeDeposito)session.load(SolicitudDeDeposito.class, id);
					try {
						solicitud.setImportado(new Date());				
						target.replicate(solicitud, ReplicationMode.OVERWRITE);	
						
						//source.update(solicitud);
						logger.info("Solicitud de deposito importada: "+solicitud.getId()+" Sucursal: "+sucursalId);
					} catch (Exception e) {				
						logger.error("Error importando solicitud de deposito: "+solicitud.getId()+" Sucursal: "+sucursalId+"  "+ExceptionUtils.getRootCauseMessage(e));				
					}	
					
					return null;
				}
				
			});
			/*
			List<SolicitudDeDeposito> solicitudes=source.find(hql, id);
			if(!solicitudes.isEmpty()){
				SolicitudDeDeposito solicitud=solicitudes.get(0);
				try {
					solicitud.setImportado(new Date());				
					target.replicate(solicitud, ReplicationMode.LATEST_VERSION);				
					source.update(solicitud);
					logger.info("Solicitud de deposito importada: "+solicitud.getId()+" Sucursal: "+sucursalId);
				} catch (Exception e) {				
					logger.error("Error importando solicitud de deposito: "+solicitud.getId()
							+"  "+ExceptionUtils.getRootCause(e),e);				
				}			
			}*/
		}
	}
	
	
	/**
	 * Importa la solicitud indicada de una sucursal a la base de datos central
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public void importar(final String id,final Long sucursalId){
		String hql="from SolicitudDeDeposito s where s.id=?";
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<SolicitudDeDeposito> solicitudes=source.find(hql, id);
		if(!solicitudes.isEmpty()){
			SolicitudDeDeposito solicitud=solicitudes.get(0);
			try {
				solicitud.setImportado(new Date());				
				target.replicate(solicitud, ReplicationMode.LATEST_VERSION);				
				source.update(solicitud);
				logger.info("Solicitud de deposito importada: "+solicitud.getId()+" Sucursal: "+sucursalId);
			} catch (Exception e) {				
				logger.error("Error importando solicitud de deposito: "+solicitud.getId()+" Sucursal: "+sucursalId
						+"  "+ExceptionUtils.getRootCauseMessage(e));				
			}			
		}
	}
	
	
	/**
	 * Replica todas las solicitudes pendientes
	 * 
	 */
	public void replicar(){
		for(Long sucursalId:getSucursales()){
			try {
				//replicar(sucursalId);
				replicar(sucursalId,new Date());
			} catch (Exception e) {
				logger.error("Error replicando depositos a la sucursal: "+sucursalId
						+"  "+ExceptionUtils.getRootCause(e),e);
			}
		}
	}
	
	/**
	 * Replica todas las solicitudes pendientes de una sucursal
	 * 
	 * @param sucursalId
	 */
	public void replicar(Long sucursalId){
		
		List<SolicitudDeDeposito> solicitudes=Services
			.getInstance()
			.getHibernateTemplate()
			.find("from SolicitudDeDeposito s left join fetch s.pago " +
					" where s.sucursal.id=? " +
					" and s.replicado is null and s.origen in (\'MOS\',\'CAM\') ",sucursalId);
		
		if(!solicitudes.isEmpty()){
			logger.info("Replicando "+solicitudes.size()+ " solicitudes pendientes para la sucursal: "+sucursalId);
			HibernateTemplate target =ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			for(SolicitudDeDeposito sol:solicitudes){
				try {
					sol.setReplicado(new Date());
					target.replicate(sol, ReplicationMode.OVERWRITE);
					Services.getInstance().getHibernateTemplate().update(sol);
					//actualizarImportado(sol);
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println(sol.getId());
					//logger.error("Error replicando solicitud de deposito: "+sol.getId()+ " a la sucursal: "+sucursalId+"  "+ExceptionUtils.getRootCauseMessage(e));	
				}
			}
		}
		
	}
	
	public void replicar(Long sucursalId,final Date fecha){
		
		List<SolicitudDeDeposito> solicitudes=Services
			.getInstance()
			.getHibernateTemplate()
			.find("from SolicitudDeDeposito s left join fetch s.pago " +
					" where s.sucursal.id=?  and date(s.fecha)=?" +
					" and s.replicado is null and s.origen in (\'MOS\',\'CAM\') ",new Object[]{sucursalId,fecha});
		
		if(!solicitudes.isEmpty()){
			logger.info("Replicando "+solicitudes.size()+ " solicitudes pendientes para la sucursal: "+sucursalId);
			HibernateTemplate target =ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			for(SolicitudDeDeposito sol:solicitudes){
				try {
					sol.setReplicado(new Date());
					target.replicate(sol, ReplicationMode.OVERWRITE);
					Services.getInstance().getHibernateTemplate().update(sol);
					//actualizarImportado(sol);
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println(sol.getId());
					//logger.error("Error replicando solicitud de deposito: "+sol.getId()+ " a la sucursal: "+sucursalId+"  "+ExceptionUtils.getRootCauseMessage(e));	
				}
			}
		}
		
	}
	
	private void actualizarImportado(SolicitudDeDeposito sol){
		String UPDATE="UPDATE sx_solicitudes_deposito SET TX_REPLICADO=? where SOL_ID=?";
		SqlParameterValue p1=new SqlParameterValue(Types.TIMESTAMP,sol.getReplicado());
		SqlParameterValue p2=new SqlParameterValue(Types.VARCHAR,sol.getId());
		Services.getInstance().getJdbcTemplate().update(UPDATE,new Object[]{p1,p2});
	}
	
	public ReplicadorDeSolicitudAutorizacionDeDeposito addSucursal(Long... sucursales){
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
		
		ReplicadorDeSolicitudAutorizacionDeDeposito replicador=new ReplicadorDeSolicitudAutorizacionDeDeposito();
		replicador.addSucursal(2L,3L,5L,6L,9L)
		//replicador.addSucursal(9L)
		.refresh();
		//replicador.replicar(5L);
		//replicador.importarForzada(3L, "03/04/2012");
		
	}

}
