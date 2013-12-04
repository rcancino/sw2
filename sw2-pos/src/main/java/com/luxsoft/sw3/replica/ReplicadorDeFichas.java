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

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador de Fichas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeFichas {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa todos las fichas pendientes desde todas las sucursales 
	 * 
	 */
	public void importar(){
		for(Long sucursalId:getSucursales()){
			try {
				importar(sucursalId);
			} catch (Exception e) {
				logger.error("Error replicando fichas de sucursal: "+sucursalId+ ExceptionUtils.getRootCauseMessage(e),e);
			}
			
		}
	}
	
	/**
	 * Importa todos las fichas <b>PENDIENTES</b> de una sucursal a la base de datos central
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
		
		List<Ficha> data=source
					.find("from Ficha x left join fetch x.partidas p " +
						  " where x.importado is null" +
						  " and x.sucursal.id=? ",sucursalId 
						  );
		if(data.size()==0)
			return;
		EventList<Ficha> eventList=GlazedLists.eventList(data);
		Comparator<Ficha> c1=GlazedLists.beanPropertyComparator(Ficha.class, "id");
		UniqueList<Ficha> pendientes=new UniqueList<Ficha>(eventList,c1);
		logger.info("Fichas pendientes por imporar: "+pendientes.size()+ " sucursal: "+sucursalId );
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Ficha ficha:pendientes){			
			try {
				ficha.setImportado(new Date());
				target.replicate(ficha, ReplicationMode.OVERWRITE);
				source.update(ficha);
				logger.info("Ficha importada: "+ficha.getId()+" Sucursal: "+sucursalId);
			} catch (Exception e) {				
				logger.error("Error importando ficha: "+ficha.getId()
						+"  "+ExceptionUtils.getRootCause(e),e);				
			}		
		}
	}
	
	
	
	/**
	 * Impporta fichas si esta no existe en producción
	 * Importa las partidas de una ficha si estas no estan en producción
	 * 
	 */
	public void importarVerificando(Long sucursalId,final Periodo periodo){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		Object[] params={sucursalId,periodo.getFechaInicial(),periodo.getFechaFinal()};
		List<Ficha> data=source
					.find("from Ficha x left join fetch x.partidas p " +
						  " where x.importado is null" +
						  " and x.sucursal.id=? " +
						  " and x.fecha between  ? and ?",params 
						  );
		if(data.size()==0)
			return;
		EventList<Ficha> eventList=GlazedLists.eventList(data);
		Comparator<Ficha> c1=GlazedLists.beanPropertyComparator(Ficha.class, "id");
		UniqueList<Ficha> pendientes=new UniqueList<Ficha>(eventList,c1);
		logger.info("Fichas pendientes por re-imporar: "+pendientes.size()+ " sucursal: "+sucursalId+ "Periodo: "+periodo.toString2() );
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		//POSDBUtils.whereWeAre();
		for(Ficha ficha:pendientes){			
			try {
				List<Ficha> foundList=target.find("from Ficha x left join fetch x.partidas p" +
						" where x.id=?",ficha.getId());
				if(foundList.isEmpty()){					
					ficha.setImportado(new Date());
					target.replicate(ficha, ReplicationMode.OVERWRITE);
					source.update(ficha);
					logger.info("Ficha nueva importada: "+ficha.getId()+" Sucursal: "+sucursalId);
				}else{
					Ficha found=foundList.iterator().next();
					ficha.setImportado(new Date());
					if(found.getPartidas().size()!=ficha.getPartidas().size()){						
						logger.info("Ficha existente inconsistente en partdas. re importando con partidas: "+ficha.getId()+" Sucursal: "+sucursalId);
						
						ficha.setIngreso(found.getIngreso());
						if(ficha.getTotal().doubleValue()<=0)
							ficha.actualizarTotal();
						target.replicate(ficha, ReplicationMode.OVERWRITE);
						ficha.setIngreso(null);
						//logger.info("Ficha nueva RE IMPORTADA: "+ficha.getId()+" Sucursal: "+sucursalId);
					}
					source.update(ficha);						
					logger.info("Ficha YA IMPORTADA Y CORRECTA: "+ficha.getId()+" Sucursal: "+sucursalId);
				}
				
				//logger.info("Ficha importada: "+ficha.getId()+" Sucursal: "+sucursalId);
			} catch (Exception e) {				
				logger.error("Error importando ficha: "+ficha.getId()
						+"  "+ExceptionUtils.getRootCauseMessage(e),e);
				eliminarFichaErronea(ficha.getId());
			}		
		}
	}
	
	/**
	 * Borra una ficha incorrectamente importada por culpa de un bug de Hibernate-MySQL
	 * @param id
	 */
	private void eliminarFichaErronea(String id){
		String DELETE="DELETE FROM SX_FICHASDET WHERE FICHA_ID=?";
		Services.getInstance().getJdbcTemplate().update(DELETE,new Object[]{id});
		DELETE="DELETE FROM SX_FICHAS WHERE FICHA_ID=?";
		Services.getInstance().getJdbcTemplate().update(DELETE,new Object[]{id});
		logger.info("Ficha eliminada: "+id);
	}
	
	public void importarVerificando(){
		importarVerificando(Periodo.getPeriodo(-1));
	}
	
	public void importarVerificando(Periodo periodo){
		for(Long sucursalId:getSucursales()){
			importarVerificando(sucursalId, periodo);
		}
	}
	
	public ReplicadorDeFichas addSucursal(Long... sucursales){
		getSucursales().clear();
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
		
		ReplicadorDeFichas replicador=new ReplicadorDeFichas();
		//replicador.addSucursal(6L)
		replicador.addSucursal(2L)
		.importarVerificando(new Periodo("20/07/2011","25/07/2011"))
		//.importarVerificando();
		;
	
		
		
	}

}
