package com.luxsoft.sw3.replica.jobs;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.replica.ReplicadorDeDepositos2;
import com.luxsoft.sw3.services.Services;

/**
 * Tarea
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class DepositosReplicaJob {
	
	private HibernateTemplate targetTemplate;
	
	private Set<Long> sucursales;
	
	private Logger logger=Logger.getLogger(getClass());
	
	private ReplicadorDeDepositos2 replicador;
	
	
	/**
	 * Replica los depositos pendientes de autorizacion
	 * Busca para todas las registradas en la propiedad de sucursales
	 *  los depositos pendientes de autorizacion y los refresca de la base central
	 *  de operaciones
	 *
	 */
	public void refresh(){
		//System.out.println("Iniciando refresh ");
		System.out.println("Sucursales: "+sucursales);
		for(Long sucursalId:sucursales){
			//refresh(sucursalId);
			refreshSucursal(sucursalId);
		}
		logger.info("Job Terminado.....................................");
	}
	
	public void refreshSucursal(final Long sucursalId){
		Date time=new Date();
		String pattern="Replicando (Refresh) sucursl: {0} inicio: {1,date,short} {1,time,short}";
		logger.info(MessageFormat.format(pattern, sucursalId,time));
		try {
			HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			getReplicador().refrescar(sucursalId, source, getTargetTemplate());		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public ReplicadorDeDepositos2 getReplicador() {
		if(replicador==null)
			replicador=new ReplicadorDeDepositos2();
		return replicador;
	}

	public void setReplicador(ReplicadorDeDepositos2 replicador) {
		this.replicador = replicador;
	}

	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}

	public HibernateTemplate getTargetTemplate() {
		if(targetTemplate==null)
			setTargetTemplate(Services.getInstance().getHibernateTemplate());
		return targetTemplate;
	}

	public void setTargetTemplate(HibernateTemplate targetTemplate) {
		this.targetTemplate = targetTemplate;
	}
	
	

}
