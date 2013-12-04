package com.luxsoft.siipap.service.aop;

import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.model.AddressLoggable;
import com.luxsoft.sw3.model.AdressLog;

/**
 * Aspecto para actualizar las direcciones fisicas 
 * 
 * @author ruben
 *
 */
public class AdressLogAspect implements SaveOrUpdateEventListener{
	
	public  String getIPAdress(){
		return KernellSecurity.getIPAdress();
	}
	
	public  String getMacAdress(){
		return KernellSecurity.getMacAdress();
	}
	
	public void registerAddress(Object bean){
		/*if(bean instanceof AddressLoggable){
			AddressLoggable al=(AddressLoggable)bean;
			AdressLog log=new AdressLog();
			String ip=getIPAdress();
			String mac=getMacAdress();
			log.setCreatedIp(ip);
			log.setCreatedMac(mac);
			log.setUpdatedIp(ip);
			log.setUpdatedIp(mac);
			al.setAddresLog(log);
		}*/
	}

	public void onSaveOrUpdate(SaveOrUpdateEvent event)	throws HibernateException {
		Object bean=event.getEntity();
		if(bean instanceof AddressLoggable){
			AddressLoggable al=(AddressLoggable)bean;
			AdressLog log=new AdressLog();
			String ip=getIPAdress();
			String mac=getMacAdress();
			log.setCreatedIp(ip);
			log.setCreatedMac(mac);
			log.setUpdatedIp(ip);
			log.setUpdatedIp(mac);
			al.setAddresLog(log);
		}
		
	}

}
