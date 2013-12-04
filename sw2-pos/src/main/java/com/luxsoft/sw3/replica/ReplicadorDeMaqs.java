package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.util.DateUtil;

import com.luxsoft.sw3.services.Services;

/**
 * Replicador de recpciones de maquila {@link RecepcionDeMaquila}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeMaqs {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa todos las recepciones pendientes desde todas las sucursales 
	 * 
	 */
	public void importar(){
		for(Long sucursalId:getSucursales()){
			try {
				importar(sucursalId);
			} catch (Exception e) {
				logger.error("Error replicando maqs de sucursal: "+sucursalId+ ExceptionUtils.getRootCauseMessage(e),e);
			}
			
		}
	}
	
	/**
	 * Importa todos las recepciones <b>PENDIENTES</b> de una sucursal a la base de datos central
	 *  
	 * 
	 * @param fecha
	 * @param sucursalId
	 * @param origen
	 * @param sourceTemplate
	 * @param targetTemplate
	 */
	public void importar(Long sucursalId){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> pendientes=source
					.find("select x.id from RecepcionDeMaquila x " +
						  " where x.importado is null " 
						  );
		for(String id:pendientes){
			
			importar(id, sucursalId);
		}
	}
	
	/**
	 * Importa la recepcion indicada de una sucursal a la base de datos central
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public void importar(final String id,final Long sucursalId){
			String hql="from RecepcionDeMaquila  e left join fetch e.partidas where e.id=?"
		;
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<RecepcionDeMaquila> recepciones=source.find(hql, id);
		if(!recepciones.isEmpty()){
			RecepcionDeMaquila recepcion=recepciones.get(0);
			try {
				recepcion.setImportado(new Date());
				// Para indicar q no se ha modificado en produccion
				recepcion.setReplicado(new Date()); 
				target.replicate(recepcion, ReplicationMode.LATEST_VERSION);
				recepcion.setReplicado(null);
				source.update(recepcion);
				logger.info("Recepcion importada: "+recepcion.getId()+" Sucursal: "+sucursalId);
			} catch (Exception e) {				
				logger.error("Error importando embarque: "+recepcion.getId()
						+"  "+ExceptionUtils.getRootCause(e),e);				
			}			
		}
	}
	
	/**
	 * Importa un grupo de recepciones
	 * 
	 * @param sucursalId
	 * @param source
	 * @param target
	 * @param ids
	 */
	public void importar(final Long sucursalId,String...ids){
		int row=0;
		for(String id:ids){
			importar(id,sucursalId);
			logger.info("Row: "+row+" de: "+ids.length);
			row++;
		}
	}
	
	public ReplicadorDeMaqs addSucursal(Long... sucursales){
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
		
		ReplicadorDeMaqs replicador=new ReplicadorDeMaqs();
		replicador.addSucursal(5L)
		//replicador.addSucursal(2L)
		//.importar(6L,"8a8a8282-290e764a-0129-0e78c90a-0001")
		.importar();
		;
		
	}

}
