package com.luxsoft.siipap.gastos;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.swing.AbstractApplicationStarter;


public class Gastos extends AbstractApplicationStarter{
		

	@Override
	protected String[] getContextPaths() {
		return new String[]{				
				"swx-gas-ctx.xml"
				,"swx-gas-actions-ctx.xml"
		};
	}
	
	@Override
	protected ApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				getContextPaths()
				);
		
	}
	
	public static void main(String[] args) {
		new Gastos().start();
	}
	
}
