package com.luxsoft.siipap.service.aop;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.replica.aop.ExportadorManager;
import com.luxsoft.siipap.service.aop.EntityModificationEvent.EntityEventType;

/**
 * Spring managed bean que se encarga de anunciar una cambios al contexto
 * cuando un bean es creado-modificado
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PersistenceNotificator implements PreInsertEventListener
									,PostInsertEventListener
									,PostUpdateEventListener
									,PostDeleteEventListener{
	
	private Logger logger=Logger.getLogger(getClass());
	
	ExportadorManager manager=ExportadorManager.getInstance();
	
	private List<EntityModificationListener> listeners=new ArrayList<EntityModificationListener>();
	
	

	public boolean onPreInsert(PreInsertEvent event) {
		try {
			//TODO Implementar el proceso de bitacora
			if(logger.isDebugEnabled()){
				logger.debug("About to insert: "+event.getEntity());
			}
			//addressAspect.registerAddress(event.getEntity());
		} catch (Exception e) {
			
		}
		return false;
	}

	public void onPostInsert(PostInsertEvent event) {		
		if(logger.isDebugEnabled()){
			String pattern="Entidad registrada a: {0} id:{1}";
			logger.debug(MessageFormat.format(pattern, event.getEntity().getClass().getName(),event.getId()));
		}
		if(event.getEntity().getClass()==Existencia.class){
			Existencia exis=(Existencia)event.getEntity();
			try {
				manager.exportarExistencia(exis, null);
				
			} catch (Exception e) {
				logger.error("Error al actualizar exis en SIIPAP ",e);
			}
		}
		final EntityModificationEvent pvent=new EntityModificationEvent(this,event.getEntity().getClass(),event.getId());
		pvent.setType(EntityEventType.INSERT);
		pvent.setTime(new Date());
		fireEvent(pvent);
		
		
	}

	public void addListener(final EntityModificationListener l){
		listeners.add(l);
	}
	public void removeListener(final EntityModificationListener l){
		listeners.remove(l);
	}

	public void onPostUpdate(PostUpdateEvent event) {
		
		if(logger.isDebugEnabled()){
			String pattern="Entidad modificada a: {0} id:{1}";
			logger.debug(MessageFormat.format(pattern, event.getEntity().getClass().getName(),event.getId()));
		}
		if(event.getEntity().getClass()==Existencia.class){
			Existencia exis=(Existencia)event.getEntity();
			try {
				
				manager.exportarExistencia(exis, null);
			} catch (Exception e) {
				logger.error("Error al actualizar exis en SIIPAP ",e);
			}
		}
		final EntityModificationEvent pvent=new EntityModificationEvent(this,event.getEntity().getClass(),event.getId());
		pvent.setType(EntityEventType.UPDATE);
		pvent.setTime(new Date());
		fireEvent(pvent);
		
	}

	public void onPostDelete(PostDeleteEvent event) {
		if(logger.isDebugEnabled()){
			String pattern="Entidad eliminada : {0} id:{1}";
			logger.debug(MessageFormat.format(pattern, event.getEntity().getClass().getName(),event.getId()));
		}
		final EntityModificationEvent pvent=new EntityModificationEvent(this,event.getEntity().getClass(),event.getId());
		pvent.setType(EntityEventType.DELETE);
		pvent.setTime(new Date());
		fireEvent(pvent);
		
	}
	
	/**
	 * En virtud de que no podemos permitir que un listener detenga el proceso de
	 * persistencia (y de que no estamos seguros de estar usando el hibernate listener
	 * adecuado al final de la transaccion) los listeneres son detonados dentro del try catch
	 * 
	 * @param e
	 */
	private void fireEvent(final EntityModificationEvent e){
		for(EntityModificationListener ln:listeners){
			try {
				ln.onEntityModification(e);
			} catch (Exception e2) {
				logger.error("Error propagando evento al listener: "+ln.toString(), e2);
			}
			
		}
	}

}
