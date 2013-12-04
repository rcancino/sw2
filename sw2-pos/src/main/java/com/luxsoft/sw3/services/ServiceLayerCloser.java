package com.luxsoft.sw3.services;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.luxsoft.siipap.swing.EventType;
import com.luxsoft.siipap.swing.SwingApplicationEvent;

/**
 * Se encarga de terminar correctamente el Service layer: {@link Services} 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ServiceLayerCloser implements ApplicationListener{

	Logger logger=Logger.getLogger(getClass());
	
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof SwingApplicationEvent){
			SwingApplicationEvent se=(SwingApplicationEvent)event;
			if(se.getType().equals(EventType.APPLICATION_CLOSED)){
				logger.info("Cerrando ServiceLayer para POS");
				Services.close();
			}
		}
		
	}

}
