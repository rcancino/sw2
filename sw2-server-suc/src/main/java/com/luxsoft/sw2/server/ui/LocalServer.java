package com.luxsoft.sw2.server.ui;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;
import com.luxsoft.sw2.server.services.LocalServerManager;
import com.luxsoft.utils.LoggerHelper;

public class LocalServer extends AbstractApplicationStarter{
	
	Logger logger=LoggerHelper.getLogger();
	
	@Override
	protected String[] getContextPaths() {
		return new String[]{				
				 "spring/server-local-ui-context.xml"
				,"spring/server-local-actions-context.xml"
				,"classpath*:com/luxsoft/sw2/server/**/consultas-local-server.xml"
		};
	}
	
	
	
	@Override
	protected void configureActions() {
		LocalServerManager.getInstance();
		super.configureActions();
	}
	
	
	
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()
				);
		
	}
	
	public static void main(String[] args) {		
		new LocalServer().start();
	}

}
