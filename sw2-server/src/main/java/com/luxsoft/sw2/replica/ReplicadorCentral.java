package com.luxsoft.sw2.replica;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteRow2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.replica.ReplicaMessageCreator;
import com.luxsoft.utils.LoggerHelper;

public class ReplicadorCentral implements ReplicaManager{
	
	Logger logger=LoggerHelper.getLogger();
	ReplicaMessageCreator messageCreator;
	
	

	public void replicar(Collection beans) {
		for(Object bean:beans){
			replicar(bean);
		}
	}

	
	public void replicar(Object bean) {
		if(messageCreator==null){
			messageCreator=new ReplicaMessageCreator();
			messageCreator.setJmsTemplate(ServiceLocator2.getJmsTemplate());
		}
		try {
			//final Object entity=resolver(bean);
			final Serializable id=resolverId(bean);
			final Class clazz=resolverClase(bean);
			ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,SQLException {					
					Object entity=session.load(clazz, id);
					entity=((HibernateProxy)entity).getHibernateLazyInitializer().getImplementation();
					EntityLog entityLog=new EntityLog(entity,id,"",EntityLog.Tipo.CAMBIO);
					messageCreator.enviar(entityLog);		
					return null;
				}
			});
			
		} catch (Exception e) {
			String message=MessageFormat.format("Error enviando replicando entidad {0}  causa: {1}"
					,bean,ExceptionUtils.getRootCauseMessage(e));
			logger.error(message);
			throw new RuntimeException(message);
		}		
	}
	
	protected Class resolverClase(final Object bean){
		if(bean instanceof ClienteRow2){
			return Cliente.class;
		}
		return bean.getClass();
	}
	
	protected Serializable resolverId(final Object bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		if(bean instanceof ClienteRow2){
			ClienteRow2 row=(ClienteRow2)bean;
			return row.getCliente_id();
		}
		return (Serializable)PropertyUtils.getProperty(bean, "id");
	}

	
	public void replicaBatch(Collection beans) {
		
		
	}
	

}
