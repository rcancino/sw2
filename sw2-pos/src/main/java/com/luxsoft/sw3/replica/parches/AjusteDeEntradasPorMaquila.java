package com.luxsoft.sw3.replica.parches;

import java.util.List;

import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;

/**
 * Replica un grupo de Recepciones de maquila generadas
 * en las oficinas y los manda a las sucursales, eliminando los COM's existentes
 * q eran la entrada original
 * @author Ruben Cancino Ramos
 *
 */
public class AjusteDeEntradasPorMaquila {
	
	
	public static void execute(){
		String hql="from RecepcionDeMaquila r left join fetch r.partidas p where  r.sucursal.id=6";
		List<RecepcionDeMaquila> data=Services
			.getInstance().getHibernateTemplate()
			.find(hql);
		EventList<RecepcionDeMaquila> source=GlazedLists.eventList(data);
		UniqueList<RecepcionDeMaquila> recepciones=new UniqueList<RecepcionDeMaquila>(source,GlazedLists.beanPropertyComparator(RecepcionDeMaquila.class, "id"));
		for(RecepcionDeMaquila r:recepciones){
			Long sucursalId=r.getSucursal().getId();
			System.out.println("Replicando maquila: "+r.getId()+ "  Sucursal: "+sucursalId);
			try {
				
				HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
				target.replicate(r, ReplicationMode.EXCEPTION);
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
 	}
	
	public static void main(String[] args) {
		execute();
	}

}
