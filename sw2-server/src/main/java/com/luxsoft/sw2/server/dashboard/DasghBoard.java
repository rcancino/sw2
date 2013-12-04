package com.luxsoft.sw2.server.dashboard;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;
import com.luxsoft.utils.LoggerHelper;

public class DasghBoard extends AbstractApplicationStarter{
	
	Logger logger=LoggerHelper.getLogger();
	
	@Override
	protected String[] getContextPaths() {
		return new String[]{				
				 "config/sw2-server-ui-context.xml"
				,"config/sw2-server-actions-context.xml"
				,ClassUtils.addResourcePathToPackagePath(DasghBoard.class, "dashboard-context.xml")
		};
	}	
	
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()
				);
		
	}
	
	public static void main(String[] args) {		
		new DasghBoard().start();
	}

}
