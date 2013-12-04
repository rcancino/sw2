package com.luxsoft.sw3.replica;

import java.util.Comparator;
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
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;

/**
 * Replicador para entregas unitarias de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeEntregas {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	public void importar(final Date fecha){
		for(Long sucursalId:getSucursales()){
			try {
				importar(sucursalId,fecha);
			} catch (Exception e) {
				logger.error("Imposible importar entregas de la sucursal: "
						+sucursalId+ " Err: \n"+ExceptionUtils.getRootCauseMessage(e),e);
				//e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Importa todas las  entregas <b>PENDIENTES</b> de una sucursal a la base de datos central
	 * 
	 * @param sucursalId
	 * @param fecha
	 */
	public void importar(Long sucursalId,final Date fecha){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> pendientes=source
					.find("select x.id from Entrega x " +
						  " where x.fecha=?",fecha);
		for(String id:pendientes){
			importar(id, sucursalId);
		}
	}
	
	/**
	 * Importa todos las entregas pendientes desde todas las sucursales 
	 * 
	 */
	public void importar(){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId);
		}
	}
	
	/**
	 * Importa todas las  entregas <b>PENDIENTES</b> de una sucursal a la base de datos central
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
					.find("select x.id from Entrega x " +
						  " where x.importado is null" +
						  "  and x.embarque.fecha>=?"+
						  "  and x.embarque.regreso is not null"
						  ,DateUtil.toDate("4/06/2010"));
		for(String id:pendientes){
			try {
				importar(id, sucursalId);
			} catch (Exception e) {				
				logger.error("Error importando entrega: "+id
						+"  "+ExceptionUtils.getRootCause(e),e);				
			}		
			
		}
	}
	
	/**
	 * Importa la entrega indicada de una sucursal a la base de datos central
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public void importar(final String id,final Long sucursalId){
			String hql="from Entrega e" +
					" left join fetch e.partidas eu" +
					"  where e.id=?"
		;
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		EventList<Entrega> entregas=GlazedLists.eventList(source.find(hql, id));
		Comparator<Entrega> c=GlazedLists.beanPropertyComparator(Entrega.class,"id");
		entregas=new UniqueList<Entrega>(entregas,c);
		if(!entregas.isEmpty()){
			Entrega entrega=entregas.get(0);
			entrega.setImportado(new Date());				
			entrega.setReplicado(new Date());// Para indicar q no se ha modificado en produccion
			InstruccionDeEntrega ie=entrega.getInstruccionDeEntrega();
			entrega.setInstruccionDeEntrega(null); //Por el momento no se pueden importar instrucciones de entrega
			target.replicate(entrega, ReplicationMode.LATEST_VERSION);
			entrega.setReplicado(null);
			entrega.setInstruccionDeEntrega(ie); //Regresamos la Instruccion
			source.update(entrega);
			logger.info("Entrega importada: "+entrega.getId());
					
		}
	}
	
	/**
	 * Importa un grupo de Entregas
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
	
	public ReplicadorDeEntregas addSucursal(Long... sucursales){
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
		
		ReplicadorDeEntregas replicador=new ReplicadorDeEntregas();
		replicador.addSucursal(2L,3L,5L,6L)		
		.importar()
		//.importar("8a8a8484-28caa0e9-0128-cb12f05e-0009", 5L)
		;
		
	}

}
