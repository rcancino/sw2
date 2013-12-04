package com.luxsoft.siipap.tesoreria;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;


public class Tesoreria extends AbstractApplicationStarter{
	
	
		

	@Override
	protected String[] getContextPaths() {
		return new String[]{				
				"swx-tes-ctx.xml"
				,"classpath*:com/luxsoft/sw3/tesoreria/**/swx-ui-context.xml"
				,"swx-tes-actions-ctx.xml"
				
		};
	}
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()
				//,ServiceLocator.getDaoContext()
				);
		
	}
	
	

	public static void main(String[] args) {
		new Tesoreria().start();
	}
	
}
