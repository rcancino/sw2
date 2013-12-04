package com.luxsoft.sw3.replica;

import java.text.MessageFormat;
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


import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador para las transformaciones de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeTransformaciones {
	
	private Logger logger=Logger.getLogger(getClass());
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa todos las transformaciones <b>PENDIENTES</b> de una sucursal a la base de datos central
	 *
	 *  
	 * 
	 * @param fecha
	 * @param sucursalId
	 * @param sourceTemplate
	 * @param targetTemplate
	 */
	public void importarPendientes(
			final Date fecha
			,Long sucursalId			
			,HibernateTemplate sourceTemplate
			,HibernateTemplate targetTemplate){
		
		throw new UnsupportedOperationException("TO BE IMPLELEMENTED");
		
	}
	
	public void importar(final Date fecha,Long sucursalId){
		HibernateTemplate souce=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		importar(fecha,sucursalId,souce,target);
	}
	
	/**
	 * Importa <b>TODAS LAS TRANSFORMACIONES DE INVENTARIO (TRS) DE UN DIA</b> de una sucursal a la base de datos central
	 * para el tipo de venta indicado
	 *  
	 * 
	 * @param fecha
	 * @param sucursalId
	 * @param sourceTemplate
	 * @param targetTemplate
	 */
	public void importar(
			final Date fecha
			,Long sucursalId			
			,HibernateTemplate sourceTemplate
			,HibernateTemplate targetTemplate){
		
		logger.info(MessageFormat.format(
						"Importando  transformaciones {0,date,short} de la sucursal: {1}"
						,fecha,sucursalId));
		String hql="select t.id from Transformacion t where t.sucursal.id=? and t.fecha=? ";	
		
		List<String> res=sourceTemplate.find(hql, new Object[]{sucursalId,fecha});
		
		for(String id:res){			
			importar(id, sucursalId, sourceTemplate, targetTemplate);
		}
	}	
	
	/**
	 * Importa la transformacion solicitado 
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public void importar(final String id
			,final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target){
			String hql="from Transformacion t left join fetch t.partidas tr where t.id=?"
		;
		List<Transformacion> data=source.find(hql, id);
		EventList<Transformacion> eventList=GlazedLists.eventList(data);
		UniqueList<Transformacion> trs=new UniqueList<Transformacion>(eventList,GlazedLists.beanPropertyComparator(Transformacion.class,"id"));
		if(!trs.isEmpty()){
			Transformacion  t=trs.get(0);
			
			try {
				target.replicate(t, ReplicationMode.OVERWRITE);
				for(TransformacionDet det:t.getPartidas()){
					if(det.getDestino()!=null){
						target.replicate(det.getDestino(), ReplicationMode.LATEST_VERSION);
						target.replicate(det, ReplicationMode.LATEST_VERSION);
						
					}
				}
				logger.info("Transformacion replicadd: "+t.getId());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(ExceptionUtils.getRootCause(e),e);
				
			}
			
		}
	}
	
	
	public ReplicadorDeTransformaciones addSucursal(Long... sucursales){
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
	
	
	public void importar(){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId);
		}
		
	}
	
	public void importar(Long sucursalId){
		
		String hql="from Transformacion t left join fetch t.partidas p where t.importado=null and t.fecha>?";
		final HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		final HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<Transformacion> data=source.find(hql, DateUtil.toDate("30/04/2011"));
		EventList<Transformacion> eventList=GlazedLists.eventList(data);
		UniqueList<Transformacion> trans=new UniqueList<Transformacion>(eventList,GlazedLists.beanPropertyComparator(Transformacion.class,"id"));
		if(trans.size()>0){
			logger.info("Importando transformaciones pendientes de la sucursal: "+sucursalId+ " trnas:"+trans.size());
		}
		for(Transformacion t:trans){
			try {
				//t.setImportado(new Date());
				target.replicate(t, ReplicationMode.OVERWRITE);
				source.update(t);
				logger.info("Transformacion Importada: "+t.getId());
				for(TransformacionDet det:t.getPartidas()){
					if(det.getDestino()!=null){
						target.replicate(det.getDestino(), ReplicationMode.LATEST_VERSION);
						target.replicate(det, ReplicationMode.LATEST_VERSION);
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(ExceptionUtils.getRootCause(e),e);
			}
		}
	}
	
	
	
	
	public static void main(String[] args) {
		ReplicadorDeTransformaciones replicador=new ReplicadorDeTransformaciones();
		HibernateTemplate targetTemplate=Services.getInstance().getHibernateTemplate();
		
		Periodo p=new Periodo("01/03/2012","31/03/2012");
		List<Date> dias=p.getListaDeDias();	
		Long[] sucursales={2l,3l,5l,6l,9l};
		for(Long sucursalId:sucursales){
			HibernateTemplate sourceTemplate=ReplicaServices
			.getInstance()
			.getHibernateTemplate(sucursalId);
			for(Date fecha:dias){
				replicador.importar(fecha, sucursalId,sourceTemplate, targetTemplate);
			}
		}
		
	}

}
