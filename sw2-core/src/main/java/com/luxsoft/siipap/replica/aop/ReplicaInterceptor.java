package com.luxsoft.siipap.replica.aop;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.BeanUtils;

import com.luxsoft.siipap.model.EntityUserLog;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.replica.ReplicaExporter.Tipo;
import com.luxsoft.siipap.service.LoginManager;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Hibernate Interceptor implementation q' se encarga de generar
 * archivos de replica para el sispap dbf, para las entidades que asi
 * lo requieran
 * 
 * TODO Trasladar la responsabilidad de decidir que entidad requiere replica
 * al ExportManager
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicaInterceptor extends EmptyInterceptor{
	
	private Set inserts=new HashSet();
	private Set deletes=new HashSet();
	private Set updates=new HashSet();
	private Logger logger=Logger.getLogger(getClass());
	
	private List<Class> clases=new ArrayList<Class>();
	private ExportadorManager exportadorManager;
	
	
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {		
		if (clases.contains(entity.getClass())){
			inserts.add(entity);
		}
		updateLog(entity);
		return false;
	}
	
	


	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {		
		if (clases.contains(entity.getClass())){
			updates.add(entity);
		}
		return false;
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if (clases.contains(entity.getClass())){
			deletes.add(entity);
		}	
	}
	
	@Override
	public void postFlush(Iterator entities) {
		try{
			
			for(Iterator it=inserts.iterator();it.hasNext();){
				logger.info("Exportando altas a siipap para ");
				final Object entity=it.next();
				getExportadorManager().exportar(entity,Tipo.A);
			}
			
			for(Iterator it=updates.iterator();it.hasNext();){
				logger.info("Exportando cambios a siipap para ");
				final Object entity=it.next();
				getExportadorManager().exportar(entity,Tipo.C);
			}
			
			for(Iterator it=deletes.iterator();it.hasNext();){
				logger.info("Exportando bajas a siipap para ");
				final Object entity=it.next();
				//getExportadorManager().exportar(entity,Tipo.D);
			}
			
			
		}catch (Exception e) {
			logger.error("Error en interceptor central de replica",e);
		}finally{
			inserts.clear();
			deletes.clear();
			updates.clear();
		}
	}
	

	public List<Class> getClases() {
		return clases;
	}


	public void setClases(List<Class> clases) {
		this.clases = clases;
	}


	public ExportadorManager getExportadorManager() {
		return exportadorManager;
	}

	public void setExportadorManager(ExportadorManager exportadorManager) {
		this.exportadorManager = exportadorManager;
	}
	
	protected void updateLog(Object entity){
		try {
			if(entity instanceof EntityUserLog){
				User user=LoginManager.getCurrentUser();
				if(user!=null){
					EntityUserLog log=(EntityUserLog)entity;
					if(log.getLog()!=null){
						logger.info("Entidad generada  "+ClassUtils.getShortClassName(entity.getClass())+"  Usuario: "+user.getUsername());
						
						Date date=ServiceLocator2.obtenerFechaDelSistema();
						log.getLog().setUpdateUser(user.getUsername());
						log.getLog().setModificado(date);
						log.getLog().setCreado(date);
						log.getLog().setCreateUser(user.getUsername());
						
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	

}
