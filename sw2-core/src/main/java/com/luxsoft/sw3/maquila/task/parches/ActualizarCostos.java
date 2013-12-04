package com.luxsoft.sw3.maquila.task.parches;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;
import com.luxsoft.sw3.maquila.model.SalidaDeHojasDet;

/**
 * Actualiza los costos de la maquila partiendo del 
 * costo de la entrada de maquila unitaria
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarCostos {
	
	
	public static void execute(Long entradaId){
		
		List<EntradaDeMaterialDet> entradas=ServiceLocator2
			.getHibernateTemplate().find("from EntradaDeMaterialDet e " +
					" where e.id=? order by e.id asc ",entradaId);
		System.out.println("Entradas a procesar: "+entradas.size());
		for(EntradaDeMaterialDet e:entradas){
			ServiceLocator2.getMaquilaManager().actualiarCostos(e);
			System.out.println("Entrada actualizada: "+e.getId());
		}
	}
	
	public static void execute(){
		
		List<EntradaDeMaterialDet> entradas=ServiceLocator2
			.getHibernateTemplate().find("from EntradaDeMaterialDet e order by e.id asc ");
		System.out.println("Entradas a procesar: "+entradas.size());
		for(EntradaDeMaterialDet e:entradas){
			ServiceLocator2.getMaquilaManager().actualiarCostos(e);
			System.out.println("Entrada actualizada: "+e.getId());
		}
	}
	
	public static void actualizarCostosDeMaquila(final int year,final int mes){
		System.out.println("Actualizando costos de maquila "+year+" / "+mes);
		final List<Long> pendientes=new ArrayList<Long>();
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				ScrollableResults rs=session.createQuery(
						" from RecepcionDeCorteDet e " +
						" where year(e.fecha)=? " +
						//"   and month(e.fecha)=?" +
						"   and e.costo=0" 
						).setParameter(0, year)
						//.setParameter(1, mes)
						.scroll();
				int buff=0;
				
				while(rs.next()){
					RecepcionDeCorteDet rr=(RecepcionDeCorteDet)rs.get()[0];
					pendientes.add(rr.getOrigen().getId());
					if((++buff)%20==0){
						System.out.println("Limpiando cache...");
						session.flush();
						session.clear();
					}
				}
				
				rs=session.createQuery(
						" from SalidaDeHojasDet s " +
						" where year(s.fecha)=? " +
						"   and s.costo=0" 
						).setParameter(0, year)
						.scroll();
				
				while(rs.next()){
					SalidaDeHojasDet sal=(SalidaDeHojasDet)rs.get()[0];
					pendientes.add(sal.getOrigen().getOrigen().getId());
					if((++buff)%20==0){
						System.out.println("Limpiando cache...");
						session.flush();
						session.clear();
					}
				}
				
				return null;
			}
		});
		for(Long id:pendientes){
			execute(id);
		}
	}
	
	public static void main(String[] args) {
		//execute();
		DBUtils.whereWeAre();
		//actualizarCostosDeMaquila(2011, 8);
		//execute(200L);
		actualizarCostosDeMaquila(2013, 8);
	}

}
