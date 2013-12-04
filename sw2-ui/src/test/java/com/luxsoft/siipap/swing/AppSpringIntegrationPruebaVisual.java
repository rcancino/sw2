package com.luxsoft.siipap.swing;


public class AppSpringIntegrationPruebaVisual {
	
	public static void main(String[] args) {
		
		AbstractApplicationStarter app=new AbstractApplicationStarter(){

			@Override
			protected String[] getContextPaths() {
				return SWResources.TEXT_CONTEXTS;
			}
			
		};		
		app.start();
	}

}
