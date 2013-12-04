package com.luxsoft.sw3.replica;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador para los movimientos de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeMovimientos {
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	private Logger logger=Logger.getLogger(getClass());
	
	public void importar(){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId);
		}
		
	}
	
	public void importar(Long sucursalId){
		
		String hql="from Movimiento m left join fetch m.partidas p where m.importado=null and m.fecha>?";
		final HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		final HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<Movimiento> data=source.find(hql, DateUtil.toDate("30/11/2010"));
		EventList<Movimiento> eventList=GlazedLists.eventList(data);
		UniqueList<Movimiento> movs=new UniqueList<Movimiento>(eventList,GlazedLists.beanPropertyComparator(Movimiento.class,"id"));
		if(movs.size()>0){
			logger.info("Importando mivimientos pendientes de la sucursal: "+sucursalId+ " Movs:"+movs.size());
		}
		for(Movimiento m:movs){
			try {
				m.setImportado(new Date());
				target.replicate(m, ReplicationMode.OVERWRITE);
				source.update(m);
				logger.info("Movimiento replicadd: "+m.getId());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(ExceptionUtils.getRootCause(e),e);
			}
		}
		
	}
	
	public void importar(final Date fecha,Long sucursalId){
		HibernateTemplate souce=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		importar(fecha,sucursalId,souce,target);
	}
	
	/**
	 * Importa <b>TODAS LAS MOVIMIENTOS DE INVENTARIO (MOV) DE UN DIA</b> de una sucursal a la base de datos central
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
						"Importando  movimientos {0,date,short} de la sucursal: {1}"
						,fecha,sucursalId));
		String hql="select m.id from Movimiento m where m.sucursal.id=? and fecha=? ";	
		
		List<String> res=sourceTemplate.find(hql, new Object[]{sucursalId,fecha});
		
		for(String id:res){			
			importar(id, sucursalId, sourceTemplate, targetTemplate);
		}
	}	
	
	/**
	 * Importa el movimiento solicitado 
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
			String hql="from Movimiento m left join fetch m.partidas p where m.id=?"
		;
		List<Movimiento> data=source.find(hql, id);
		EventList<Movimiento> eventList=GlazedLists.eventList(data);
		UniqueList<Movimiento> movs=new UniqueList<Movimiento>(eventList,GlazedLists.beanPropertyComparator(Movimiento.class,"id"));
		if(!movs.isEmpty()){
			Movimiento m=movs.get(0);			
			try {
				target.replicate(m, ReplicationMode.OVERWRITE);
				logger.info("Movimiento replicadd: "+m.getId());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(ExceptionUtils.getRootCause(e),e);
				
			}
			
		}
	}
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeMovimientos addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
		}
		return this;
	}
	
	public static void main(String[] args) {
		ReplicadorDeMovimientos replicador=new ReplicadorDeMovimientos();
		/*replicador.addSucursales(2l,3l,5l,6l,9l);
		replicador.importar();*/
		
		
		HibernateTemplate targetTemplate=Services.getInstance().getHibernateTemplate();
		
		Periodo p=new Periodo("01/04/2012","30/04/2012");
		List<Date> dias=p.getListaDeDias();	
		Long[] sucursales={3L,7L,9L};
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
