package com.luxsoft.sw3.replica;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.luxsoft.utils.LoggerHelper;


/**
 * HibernateListener para generar bitacora de modificaciones en Clientes
 * 
 * PROTOTIPO AUN NO SE USA
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("rawtypes")
public class ClienteLogListener implements PostUpdateEventListener,InitializingBean{
	
	
	
	
	private HibernateTemplate hibernateTemplate;
	
	
	private Logger logger=LoggerHelper.getLogger();
	private String ip;

	public void onPostUpdate(PostUpdateEvent event) {
		audit(event.getEntity(),event.getId(),"UPDATE");
	}

	private void audit(Object entity,Serializable id,String tipo){
		
	}
	
	private void audit(AuditLog log){
		
		try {
			getHibernateTemplate().save(log);
			getHibernateTemplate().flush();
			//AuditLog res=(AuditLog)getHibernateTemplate().merge(log);
			logger.debug("Log registrado:"+log);
		} catch (Exception e) {
			logger.error(" Error registrando AuditLog: "+log+"  Causa:"
					+ ExceptionUtils.getRootCauseMessage(e),ExceptionUtils.getCause(e));
		}
	}
	
	
	
	public void afterPropertiesSet() throws Exception {
		
		Assert.notNull(hibernateTemplate,"Se requiere hibernateTemplate");
		
		try {
			ip=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {ip="NO DISPONIBLE";}
	}
	
	

	public void setSessionFactory(SessionFactory sessionFactory) {
		setHibernateTemplate(new HibernateTemplate(sessionFactory));
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	public String getIPAdress(){
		return ip;
	}	
	
	

	

}
