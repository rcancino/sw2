package com.luxsoft.sw3.replica;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador para Embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeEmbarques {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa todos los embarques pendientes desde todas las sucursales 
	 * 
	 */
	public void importar(){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId);
		}
	}
	
	/**
	 * Importa todos los  embarques <b>PENDIENTES</b> de una sucursal a la base de datos central
	 *  
	 * 
	 * @param fecha
	 * @param sucursalId
	 * @param origen
	 * @param sourceTemplate
	 * @param targetTemplate
	 */
	public synchronized void importar(Long sucursalId){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> pendientes=source
					.find("select x.id from Embarque x " +
						  " where x.importado is null and x.fecha>=?"
						  ,DateUtil.toDate("01/06/2012"
								  ));
		for(String id:pendientes){
			importar(id, sucursalId,ReplicationMode.OVERWRITE);
		}
	}
	
	public synchronized void importarNew(Long sucursalId){
		Date f1=new Date();
		Date f2=DateUtils.addDays(f1, -1);
		logger.info(MessageFormat.format("Importando embarques de la sucursal {0} del : {1,date,short}  al  {2,date,short}",sucursalId,f1,f2));
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		Object params[]={
				f1
				,f2
				};
		List<String> pendientes=source
					.find("select x.id from Embarque x " +
						  " where x.fecha between ? and ?"
						  ,params);
		for(String id:pendientes){
			importar(id, sucursalId,ReplicationMode.OVERWRITE);
		}
	}
	
	/**
	 * Importa el embarque indicado de una sucursal a la base de datos central
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public synchronized void importar(final String id,final Long sucursalId,ReplicationMode mode){
			String hql="from Embarque e  left join fetch e.partidas p where e.id=?"
		;
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<Embarque> embarques=source.find(hql, id);
		if(!embarques.isEmpty()){
			Embarque embarque=embarques.get(0);
			try {
				embarque.setImportado(new Date());
				// Para indicar q no se ha modificado en produccion
				embarque.setReplicado(new Date());
				
				eliminarEmbarque(embarque.getId(),target);
				target.replicate(embarque, mode);
				embarque.setReplicado(null);
				source.update(embarque);
				logger.info("Embarque importado: "+embarque.getId()+" Sucursal: "+sucursalId);
			} catch (Exception e) {				
				logger.error("Error importando embarque: "+embarque.getId()
						+"  "+ExceptionUtils.getRootCause(e),e);				
			}			
		}
	}
	
	private void eliminarEmbarque(final String id ,HibernateTemplate template){
		template.execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				Embarque e=(Embarque)session.get(Embarque.class, id);
				if(e!=null){
					session.delete(e);
				}
				return null;
			}
		});
	}
	
	
	/**
	 * Importa un grupo de Embarques
	 * 
	 * @param sucursalId
	 * @param source
	 * @param target
	 * @param ids
	 */
	public void importar(final Long sucursalId,String...ids){
		int row=0;
		for(String id:ids){
			importar(id,sucursalId,ReplicationMode.OVERWRITE);
			logger.info("Row: "+row+" de: "+ids.length);
			row++;
		}
	}
	
	public ReplicadorDeEmbarques addSucursal(Long... sucursales){
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
		
		ReplicadorDeEmbarques replicador=new ReplicadorDeEmbarques();
		replicador.addSucursal(3L,5L,2L,6l,9l)
		//replicador.addSucursal(6L)
		.importar();
		//replicador.importar("8a8a81e8-2a14e458-012a-15261beb-000a", 3L,ReplicationMode.OVERWRITE);
		//replicador.importar();
		
		
	}
 
}
