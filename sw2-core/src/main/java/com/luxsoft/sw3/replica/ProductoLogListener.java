package com.luxsoft.sw3.replica;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.utils.LoggerHelper;

public class ProductoLogListener implements PostUpdateEventListener{
	
	Logger logger=LoggerHelper.getLogger();
	
	private HibernateTemplate hibernateTemplate;

	public void onPostUpdate(PostUpdateEvent event) {
		if(event.getEntity() instanceof Producto){
			/*
			Producto p=(Producto)event.getEntity();
			
			
			for(Object o:event.getOldState()){
				if(o!=null)
					System.out.println(o.getClass().getName());
			}*/
			//logger.log("Old state: "+ArrayUtils.toString(event.getOldState()));
			
			System.out.println("New state: "+ArrayUtils.toString(event.getState()));
			System.out.println("User: "+KernellSecurity.instance().getCurrentUserName());
			logger.info("Old state: "+ArrayUtils.toString(event.getOldState()));
			
		}
		
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	
}
