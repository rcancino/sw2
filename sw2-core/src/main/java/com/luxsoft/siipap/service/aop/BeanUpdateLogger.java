package com.luxsoft.siipap.service.aop;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.model.EntityUserLog;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.LoginManager;

public class BeanUpdateLogger extends DefaultSaveOrUpdateEventListener implements PreUpdateEventListener,PreInsertEventListener,SaveOrUpdateEventListener{
	
	private Logger logger=Logger.getLogger(getClass());
	
	private JdbcTemplate jdbcTemplate;

	public boolean onPreUpdate(PreUpdateEvent event) {
		/*try {
			Object target=event.getEntity();
			Serializable id=event.getId();
			if(target instanceof EntityUserLog){
				logger.info("Entidad por ser actualizada Id:"+id+ "   Clase: "+target.getClass().getName());
				User user=LoginManager.getCurrentUser();
				if(user!=null){
					EntityUserLog log=(EntityUserLog)target;
					if(log.getLog()!=null){
						log.getLog().setUpdateUser(user.getUsername());
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
		}*/
		return false;
	}
	
	
	public boolean onPreInsert(PreInsertEvent event) {
		/*try {
			Object target=event.getEntity();
			Serializable id=event.getId();
			if(target instanceof EntityUserLog){				
				User user=LoginManager.getCurrentUser();
				if(user!=null){
					EntityUserLog log=(EntityUserLog)target;
					if(log.getLog()!=null){
						logger.info("Entidad por ser insertada Id:"+id+ "   Clase: "+target.getClass().getName()+ "Usuario: "+user.getUsername());
						log.getLog().setCreateUser(user.getUsername());
						log.getLog().setUpdateUser(user.getUsername());
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
		}*/
		return false;
	}


	public synchronized Date obtenerFechaDelSistema(){
		return (Date)getJdbcTemplate().queryForObject("select now()", Date.class);
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public static void main(String[] args) {
		//Method method=BeanUtils.findDeclaredMethodWithMinimalParameters(Abono.class, "getLog");
		
		/*
		PropertyDescriptor[] res=BeanUtils.getPropertyDescriptors(Abono.class);
		for(PropertyDescriptor pd:res){
			Class clazz=pd.getPropertyType();
			if(clazz.equals(UserLog.class) ){
				System.out.println("Properidad asignable: "+pd.getName());
			}
		}
		*/
	}


	public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
		
		
		try {
			Object target=event.getEntity();
			if(target instanceof EntityUserLog){
				System.out.println("Procesando event...."+event.getEntityName());
				User user=LoginManager.getCurrentUser();
				if(user!=null){
					EntityUserLog log=(EntityUserLog)target;
					if(log.getLog()!=null){
						logger.info("Entidad generada  "+event.getEntityName()+"  Usuario: "+user.getUsername());
						log.getLog().setCreateUser(user.getUsername());
						log.getLog().setUpdateUser(user.getUsername());
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
		}
		super.onSaveOrUpdate(event);
	}

}
