package com.luxsoft.siipap.pos.ui;




import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;


public class Pos extends AbstractApplicationStarter{
	
	
		

	@Override
	protected String[] getContextPaths() {
		
		return new String[]{				
				"config/swx-pos-ctx.xml"
				,"config/swx-pos-actions-ctx.xml"
				,"classpath*:com/**/swx-pos-ui*.xml"
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
		new Pos().start();
	}
	
}
