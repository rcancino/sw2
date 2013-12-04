package com.luxsoft.sw2.server.ui;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.AbstractApplicationStarter;
import com.luxsoft.sw2.server.ServerManager;

import com.luxsoft.utils.LoggerHelper;

public class SWServer extends AbstractApplicationStarter{
	
	Logger logger=LoggerHelper.getLogger();
	
	@Override
	protected String[] getContextPaths() {
		return new String[]{				
				 "config/sw2-server-ui-context.xml"
				,"config/sw2-server-actions-context.xml"
				,"classpath*:com/luxsoft/sw2/server/**/consultas-ui*.xml"
		};
	}
	
	
	
	@Override
	protected void configureActions() {
		logger.info("Iniciailizando ServiceLocator...");
		ServiceLocator2.getJdbcTemplate();
		logger.info("Iniciailizando ReplicaManager...");
		ServerManager.getReplicaManager();
		super.configureActions();
	}
	
	
	
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()
				);
		
	}
	
	public static void main(String[] args) {		
		new SWServer().start();
	}

}
