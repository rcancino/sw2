package com.luxsoft.sw3.services.parches;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.tesoreria.model.CargoAbonoPorCorte;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;

public class EliminacionDeCortesDeTarjetas {
	
	public void execute(Periodo periodo){
		String hql="select c.id from CorteDeTarjeta c where date(c.corte) between ? and ?";
		List<Long> cortes=ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
		System.out.println("Cortes a eliminar: "+cortes.size());
		for(Long id:cortes){
			System.out.println("Corte:"+id);
			execute(id);
		}
		
	}
	
	public void execute(Long corteId){
		try {
			List<CorteDeTarjeta> data=ServiceLocator2.getHibernateTemplate()
				.find("from CorteDeTarjeta c " +
						" left join fetch c.partidas part" +
						" left join fetch c.aplicaciones aplic" +
						" where c.id=?",corteId);
			if(!data.isEmpty()){					
				CorteDeTarjeta corte=data.get(0);
				
				Set<CargoAbonoPorCorte> aplicaciones=corte.getAplicaciones();
				
				corte.setAplicaciones(new HashSet<CargoAbonoPorCorte>());
				ServiceLocator2.getHibernateTemplate().update(corte);
				
				ServiceLocator2.getIngresosManager().registrarCorte(corte);
				for(CargoAbonoPorCorte det:aplicaciones){
					ServiceLocator2.getUniversalDao().remove(CargoAbono.class, det.getCargoAbono().getId());
				}
				System.out.println("Corte reprocesado: "+corte);
			}
			//ServiceLocator2.getIngresosManager().eliminarCorte(corteId);
			//System.out.println("Corte eliminado: "+corteId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		EliminacionDeCortesDeTarjetas task=new EliminacionDeCortesDeTarjetas();
		task.execute(new Periodo("01/11/2010","30/11/2010"));
		//task.execute(888L);
		//task.execute(new Periodo("01/11/2010","30/11/2010"));
	}

}
