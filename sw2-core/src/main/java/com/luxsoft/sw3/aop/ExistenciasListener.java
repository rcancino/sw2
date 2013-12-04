package com.luxsoft.sw3.aop;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.utils.LoggerHelper;

public class ExistenciasListener implements PreInsertEventListener,PreUpdateEventListener
{
	Logger logger=LoggerHelper.getLogger();
	
	
	
	public boolean onPreInsert(PreInsertEvent event) {
		final Object object = event.getEntity();
		if(object instanceof Existencia){
			String[] propertyNames=event.getPersister().getPropertyNames();
			Object[] state=event.getState();
			int index=ArrayUtils.indexOf(propertyNames, "modificado");
			if(index>=0){
				state[index]=new Date();
			}
			index=ArrayUtils.indexOf(propertyNames, "creado");
			if(index>=0){
				state[index]=new Date();
			}
		}
		return false;
	}
	
	public boolean onPreUpdate(PreUpdateEvent event) {
		
		//final SessionImplementor source = event.getSource();
		final Object object = event.getEntity();
		//final Object entity = source.getPersistenceContext().unproxyAndReassociate( object );
		if(object instanceof Existencia){
			String[] propertyNames=event.getPersister().getPropertyNames();
			Object[] state=event.getState();
			int index=ArrayUtils.indexOf(propertyNames, "modificado");
			if(index>=0){
				state[index]=new Date();
			}
		}
		
		
		//decorate(entity);
		return false;
	}
	
	private void decorate(Object bean){
		if(bean instanceof Existencia){
			logger.info("Interceptndo existencia");
			System.out.println("Interceptando existencia...");
			Existencia e=(Existencia)bean;
			e.setModificado(new Date());
			e.setCreateUser("TEST_USER");
		}
		
	}
	
	

}
