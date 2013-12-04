package com.luxsoft.siipap.swx;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;


public class SiipapEx extends AbstractApplicationStarter{
	
	
		

	@Override
	protected String[] getContextPaths() {
		return new String[]{				
				"config/swx-gas-ctx.xml"
				,"config/swx-gas-actions-ctx.xml"
				//,"config/swx-ui-compras-context.xml"
				,"classpath*:com/**/swx-ui*.xml"
				,"classpath*:com/**/swx-controllers-context.xml"
				,"classpath*:com/**/swx-views-context.xml"
				//,"classpath*:/applicationContext.xml", // for modular projects
                //,"classpath:**/swx-ui*.xml" 
		};
	}
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()				
				);
		
	}
	
	

	public static void main(String[] args) {
		new SiipapEx().start();
	}
	
}
