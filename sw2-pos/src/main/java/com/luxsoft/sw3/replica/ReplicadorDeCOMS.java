package com.luxsoft.sw3.replica;

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

import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador Statefull para importar las recepciones de compra
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicadorDeCOMS {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa todos los COMs pedientes de importacion para todas las sucursales
	 * 
	 */
	public void importar(){		
		for(Long sucursalId:getSucursales()){
			try {
				importar(sucursalId);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
	}
	
	/**
	 * Importa todos los COMs pendientes de importaion para la sucursal indicada
	 * 
	 * @param sucursalId
	 */
	public void importar(Long sucursalId){
		String hql="from RecepcionDeCompra c left join fetch c.partidas p  where c.sucursal.id=? and c.importado is null ";
		Object[] params={sucursalId};
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<RecepcionDeCompra> data=source.find(hql,params);
		EventList<RecepcionDeCompra> eventList=GlazedLists.eventList(data);
		UniqueList<RecepcionDeCompra> entradas=new UniqueList<RecepcionDeCompra>(eventList,GlazedLists.beanPropertyComparator(RecepcionDeCompra.class,"id"));
		if(entradas.size()>0){
			logger.info("Importando "+entradas.size()+ " COMS pendientes de la sucursal: "+sucursalId);
		}
		for(RecepcionDeCompra com:entradas){
			try {	
				com.setImportado(new Date());
				Services.getInstance().getHibernateTemplate().replicate(com, ReplicationMode.IGNORE);				
				source.update(com);
				logger.info("COM importada: "+com);
			} catch (Exception e) {
				logger.error("Error importando compra: "
						+com.getId()+ " de la sucursal: "+sucursalId+ "  "
						+ExceptionUtils.getRootCauseMessage(e),e);
			}
		}
	}
	
	
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ReplicadorDeCOMS addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
		}
		return this;
	}

	public static void main(String[] args) {
		ReplicadorDeCOMS replicador=new ReplicadorDeCOMS();
		replicador.addSucursales(2L,3L,6L,5L,7L)
		.importar()
		;
	}
}
