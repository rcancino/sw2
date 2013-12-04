package com.luxsoft.siipap.inventarios.service;

import org.apache.log4j.Logger;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;

import com.luxsoft.siipap.inventarios.dao.ExistenciaDao;
import com.luxsoft.siipap.inventarios.model.Inventario;

/**
 * Listener que se encarga de actualizar las 
 * existencias al registrar movimientos de inventario
 * 
 * @author Ruben Cancino
 *
 */
public class HbmExistenciaListener implements PostInsertEventListener
				,PostUpdateEventListener
				,PostDeleteEventListener
	{
	
	private boolean activo=true;
	private Logger logger=Logger.getLogger(getClass());

	private ExistenciaDao existenciaDao;
	
	public void onPostInsert(PostInsertEvent event) {
		if(!isActivo())
			return;
		if(event.getEntity() instanceof Inventario){
			Inventario inv=(Inventario)event.getEntity();
			logger.debug("Movimiento de inventario generado:"+inv);
			//existenciaDao.actualizarExistencia(inv.getProducto(), inv.getSucursal(), inv.getCantidad());
		}
	}

	public void onPostUpdate(PostUpdateEvent event) {
		if(!isActivo())
			return;
		if(event.getEntity() instanceof Inventario){
			Inventario inv=(Inventario)event.getEntity();
			//existenciaDao.actualizarExistencia(inv.getProducto(), inv.getSucursal(), inv.getCantidad());
		}
	}

	public void onPostDelete(PostDeleteEvent event) {
		if(!isActivo())
			return;
		if(event.getEntity() instanceof Inventario){
			Inventario inv=(Inventario)event.getEntity();
			//existenciaDao.actualizarExistencia(inv.getProducto(), inv.getSucursal(), inv.getCantidad());
		}
	}
	
	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public ExistenciaDao getExistenciaDao() {
		return existenciaDao;
	}

	public void setExistenciaDao(ExistenciaDao existenciaDao) {
		this.existenciaDao = existenciaDao;
	}
	
	

}
