package com.luxsoft.sw3.impap.ui;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;


public class ImpapEx extends AbstractApplicationStarter{
	
	
	
	
	@Override
	protected String[] getContextPaths() {
		return new String[]{				
				 "config/swx-impap-ctx.xml"
				,"config/swx-impap-actions-ctx.xml"
				,"classpath*:com/**/swx-compras-ui-context.xml"
				,"classpath*:com/**/swx-cxp-ui-context.xml"
				,"classpath*:com/**/swx-ventas-ui-context.xml"
				
				 
		};
	}
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()			
				);
		
	}	

	public static void main(String[] args) {
		new ImpapEx().start();
	}

}
