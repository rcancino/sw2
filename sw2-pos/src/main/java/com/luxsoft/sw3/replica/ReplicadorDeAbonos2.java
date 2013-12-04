package com.luxsoft.sw3.replica;

import java.text.MessageFormat;
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

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.services.Services;

/**
 * Importa y replica los abonos pendientes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeAbonos2 {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa los abonos correspondientes al dia del sistema
	 * utlizando modo de replicación  {@link  Replication.OVEWRITE}
	 * 
	 */
	public void importar(){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId,Periodo.hoy(),ReplicationMode.OVERWRITE);
		}
		
	}
	
	/**
	 * Importa los abonos correspondientes a un periodo
	 * 
	 * @param sucursalId  
	 * @param periodo 
	 * @param mode 
	 */
	public void importar(Long sucursalId,Periodo periodo,ReplicationMode mode){
		importar(sucursalId, mode,periodo.getListaDeDias().toArray(new Date[0]));
	}
	
	/**
	 * Importa los abonos para los dias indicados
	 * 
	 * @param sucursalId
	 * @param mode Tipo de replicación
	 * @param dias
	 */
	public void importar(final Long sucursalId,ReplicationMode mode,Date...dias){
		for(Date dia:dias){
			importar(sucursalId, dia, mode);
		}
	}
	
	/**
	 * Importa los abonos de la sucursal para la fecha indicada
	 * 
	 * @param sucursalId
	 * @param dia
	 * @param mode
	 */
	public void importar(final Long sucursalId,Date dia,ReplicationMode mode){
		
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		
		String msg=MessageFormat.format("Importando abonos pendientes del {0} para la sucursal: {1}", dia,sucursalId);
		logger.info(msg);
		
		//Abonos del dia 
		String hql="from Abono v where v.sucursal.id=? " +
				" and v.fecha=? " +
				" and v.importado is null";
		
		EventList<Abono> data=GlazedLists.eventList(source.find(hql, new Object[]{sucursalId,dia}));
		Comparator<Abono> c1=GlazedLists.beanPropertyComparator(Abono.class, "id");
		UniqueList<Abono> abonos=new UniqueList<Abono>(data,c1);
		logger.info("Abonos por importar: "+abonos.size());
		final HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		final Set<String> exitosos=new HashSet<String>();
		
		for(Abono ab:abonos){
			try {
				target.replicate(ab, mode);
				logger.info("Abono replicado: "+ab.getId());
				exitosos.add(ab.getId());
			} catch (Exception e) {				
				logger.error("Error replicando abono: "+ab.getId()
						+"  "+ExceptionUtils.getRootCause(e),e);				
			}	
		}
		actualizarImportacion(sucursalId, exitosos.toArray(new String[0]));
	}
	
	public void importarDeSaldosAFavor(final Date dia){
		/*//Abonos de aplicaciones generadas en el dia
		String hql2="select a.abono.id from Aplicacion a where a.cargo.sucursal.id=? and a.fecha=? and a.fecha>a.abono.fecha";
		Set<String> aplicIds=new HashSet<String>(source.find(hql2, new Object[]{sucursalId,dia}));
		logger.info("Abonos con aplicaciones en el dia: "+aplicIds.size());
		ids.addAll(aplicIds);
		logger.info("Total abonos por importar: "+ids.size());
		importar(sucursalId, ids.toArray(new String[0]));*/
	}
	
	/**
	 * Actualiza el campo de importado 
	 * 
	 * @param id
	 * @param sucursalId
	 */
	protected void actualizarImportacion(Long sucursalId,String...ids){
		String sql="UPDATE SX_CXC_ABONOS SET TX_IMPORTADO=? WHERE ABONO_ID IN (";
		for(int i=0;i<ids.length;i++){
			String id=ids[i];
			sql+="\'"+id+"\'";
			if(i==(ids.length-1))
				sql+=")";
			else
				sql+=",";
		}
		sql+=" AND SUCURSAL_ID=?";
	}
	
	/**
	 * Importa en forma independiente los abonos indicados
	 * 
	 * @param sucursalId
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
	
	/**
	 * Importa el abono indicado de una sucursal a la base de datos central
	 * Util para importar abonos de manera independientes
	 * 
	 * @param id
	 * @param sucursalId
	 * @param source
	 * @param target
	 */
	public void importar(final String id,final Long sucursalId){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		
			String hql="from Abono a " +
			" left join fetch a.aplicaciones ap" +			
			" where a.id=? "
		;
		List<Abono> abonos=source.find(hql, id);
		
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		
		if(!abonos.isEmpty()){
			Abono a=abonos.get(0);
			if(a instanceof NotaDeCreditoDevolucion){				
				return;				
			}
			try {
				a.setImportado(new Date());
				target.replicate(a, ReplicationMode.OVERWRITE);
				source.update(a);
				logger.info("Abono replicado: "+a.getId());
			} catch (Exception e) {				
				logger.error("Error replicando abono: "+a.getId()
						+"  "+ExceptionUtils.getRootCause(e),e);				
			}			
		}
	}
	
	
	public void replicar(){
		//Pendientes de Cargos por tesoreria
		String hql="from Aplicacion c ";
	}
	
	
	public ReplicadorDeAbonos2 addSucursal(Long... sucursales){
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
		new ReplicadorDeAbonos2()
		.addSucursal(2L,3L,5L,6L,9L)
		.importar();
		//.actualizarImportacion(3L)		;
		
	}

}
