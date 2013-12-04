package com.luxsoft.sw3.replica.parches;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;

/**
 * Baja cambios de datos en la entidad de sucursales de Produccion a las sucursales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarSucursales {
	
	
	public void execute(){
		
		Long[] sucursales={2L,3L,5L,6L,7L};
		List<Sucursal> data=Services.getInstance().getSucursalesOperativas();
		for(Long succursalId:sucursales){
			try {
				HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(succursalId);
				for(Sucursal s:data){
					Sucursal starget=(Sucursal)target.get(Sucursal.class, s.getId());
					starget.setDireccion(s.getDireccion());
					target.update(starget);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		new ActualizarSucursales().execute();
	}

}
