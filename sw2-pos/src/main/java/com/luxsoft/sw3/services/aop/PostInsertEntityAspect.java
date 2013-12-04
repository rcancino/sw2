package com.luxsoft.sw3.services.aop;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.sw3.services.Services;

public class PostInsertEntityAspect implements PreInsertEventListener{
	
	private Logger logger=Logger.getLogger(getClass());

	public boolean onPreInsert(PreInsertEvent event) {
		//System.out.println("STates: "+ArrayUtils.toString(event.getSource()));
		try {
			//TODO Implementar el proceso de bitacora
			if(logger.isDebugEnabled()){
				logger.debug("About to insert: "+event.getEntity());
			}
			if(event.getEntity() instanceof Inventario){
				Inventario i=(Inventario)event.getEntity();
				
				//System.out.println("Actualizar inventario...."+i.getProducto());
				//Services.getInstance().getInventariosManager().actualizarInventarioEnLinea(i.getClave(), i.getSucursal().getId(), i.getCantidad());
			}
		} catch (Exception e) {
			
		}
		return false;
		
		
	}

}
