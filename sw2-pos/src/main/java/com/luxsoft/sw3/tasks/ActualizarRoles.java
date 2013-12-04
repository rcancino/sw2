package com.luxsoft.sw3.tasks;

import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;



/**
 * Actualiza los roles para el punto de venta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarRoles {
	
	
	public void execute(){
		POSDBUtils.whereWeAre();
		for(POSRoles rol:POSRoles.values()){
			Role r=new Role(rol.name());
			r.setDescription(rol.name());
			r.setModulo("POS");
			try {
				Services.getInstance().getUniversalDao().save(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		new ActualizarRoles().execute();
	}

}
