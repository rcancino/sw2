package com.luxsoft.sw3.replica;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador para las devoluciones de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeDevoluciones {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * 
	 * 
	 */
	public void importarPendientes(){
		
		throw new UnsupportedOperationException("TO BE IMPLELEMENTED");
		
	}
	
	public void importar(final Date fecha,Long sucursalId){
		HibernateTemplate souce=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		importar(fecha,sucursalId,souce,target);
	}
	
	/**
	 * Importa <b>TODAS LAS DEVOLUCIONES  DE UN DIA</b> de una sucursal a la base de datos central
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
						"Importando  devoluciones {0,date,short} de la sucursal: {1}"
						,fecha,sucursalId));
		String hql="select m.id from Devolucion m where m.venta.sucursal.id=? and fecha=? ";	
		
		List<String> res=sourceTemplate.find(hql, new Object[]{sucursalId,fecha});
		
		for(String id:res){			
			importar(id, sucursalId, sourceTemplate, targetTemplate);
		}
	}	
	
	/**
	 * Importa la devolucion  solicitada 
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
			String hql="from Devolucion d " +
					" left join fetch d.partidas p " +
					" left join fetch p.nota n " +
					" where d.id=?" +
					""
		;		
		List<Devolucion> data=source.find(hql, id);
		EventList<Devolucion> eventList=GlazedLists.eventList(data);
		UniqueList<Devolucion> devs=new UniqueList<Devolucion>(eventList,GlazedLists.beanPropertyComparator(Devolucion.class,"id"));
		if(!devs.isEmpty()){
			Devolucion d=devs.get(0);			
			final UniqueList<NotaDeCredito> notas=new UniqueList<NotaDeCredito>(new BasicEventList<NotaDeCredito>(),GlazedLists.beanPropertyComparator(NotaDeCredito.class, "id"));
			
			try {
				target.replicate(d, ReplicationMode.IGNORE);
				for(DevolucionDeVenta det:d.getPartidas()){
					if(det.getNota()!=null){
						notas.add(det.getNota());
					}
				}
				logger.info("Devolucion replicada: "+d.getId());
				for(NotaDeCredito nota:notas){
					target.replicate(nota, ReplicationMode.IGNORE);
					logger.info("Abono (NotaDeCredito) replicado: "+nota.getId());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(ExceptionUtils.getRootCause(e),e);
				
			}
			
		}
	}
	
	public void importar(final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target,String...ids){
		for(String id:ids){
			importar(id,sucursalId,source,target);
		}
	}
	
	public void importarFaltantes(Periodo periodo){
		for(Long sucursalId:getSucursales()){
			importarFaltantes(sucursalId,periodo);
		}
	}
	
	public void importarFaltantes(Long sucursalId,Periodo periodo){
		importarFaltantes(sucursalId,periodo.getListaDeDias().toArray(new Date[0]));
	}
	
	public void importarFaltantes(Long sucursalId,Date...dias){
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Date dia:dias){
			String msg=MessageFormat.format("Importando devoluciones pendientes del {0} para la sucursal: {1}", dia,sucursalId);
			System.out.println(msg);
			String hql="select d.id from Devolucion d where d.venta.sucursal.id=? and d.fecha=? ";
			HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			List<String> faltantes=new ArrayList<String>();
			List<String> ids=source.find(hql, new Object[]{sucursalId,dia});
			for(String id:ids){
				Devolucion v=(Devolucion)target.get(Devolucion.class, id);
				if(v==null){
					System.out.println("Faltante ID: "+id);
					faltantes.add(id);
				}
			}
			System.out.println("Devoluciones faltantes detectados: "+faltantes.size());
			importar(sucursalId, source, target, faltantes.toArray(new String[0]));
		}
	}
	
	public ReplicadorDeDevoluciones addSucursal(Long... sucursales){
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
		
		String hql="from Devolucion d left join fetch d.partidas p where d.importado=null and d.fecha>?";
		final HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		final HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		List<Devolucion> data=source.find(hql, DateUtil.toDate("17/05/2011"));
		EventList<Devolucion> eventList=GlazedLists.eventList(data);
		UniqueList<Devolucion> devs=new UniqueList<Devolucion>(eventList,GlazedLists.beanPropertyComparator(Devolucion.class,"id"));
		if(devs.size()>0){
			logger.info("Importando devoluciones pendientes de la sucursal: "+sucursalId+ " Devs:"+devs.size());
		}
		for(Devolucion d:devs){
			try {
				d.setImportado(new Date());
				target.replicate(d, ReplicationMode.OVERWRITE);
				source.update(d);
				logger.info("Devolucion Importada: "+d.getId());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(ExceptionUtils.getRootCause(e),e);
			}
		}
	}

	
	
	
	
	public static void main(String[] args) {
		ReplicadorDeDevoluciones replicador=new ReplicadorDeDevoluciones();
		/*
		Long sucursalId=6L;
		HibernateTemplate source=Services.getInstance().getHibernateTemplate();
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		String ids[]={				
				"8a8a8283-27aaaa42-0127-aadfdaae-0001",				
				
		};
		replicador.importar(				
				 sucursalId, source, target
				 ,ids				 
				 );
		
	*/
		/*
		HibernateTemplate targetTemplate=Services.getInstance().getHibernateTemplate();
		
		Periodo p=new Periodo("01/03/2010","01/03/2010");
		List<Date> dias=p.getListaDeDias();		
		Long sucursales[]={3L,5L,2L,6L,7L};
		//Long sucursales[]={5L};
		for(Long sucursalId:sucursales){
			HibernateTemplate sourceTemplate=ReplicaServices
			.getInstance()
			.getHibernateTemplate(sucursalId);
			for(Date fecha:dias){
				replicador.importar(fecha, sucursalId,sourceTemplate, targetTemplate);
			}
		}
		*/
		
		replicador.addSucursal(2L,3L,5L,6L)
			.importarFaltantes(new Periodo("14/03/2011","18/11/2011"));
		
	}

}
