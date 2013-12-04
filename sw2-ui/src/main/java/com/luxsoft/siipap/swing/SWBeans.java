package com.luxsoft.siipap.swing;

import org.springframework.util.StringUtils;

/**
 * Enumeracion de id's de los componentes basicos y obligatorios
 * para crear una aplicacion Siipap-Swing
 * 
 * Esta enumeracion es particularmente util para accesar estos componenetes
 * en que se hospedan en un contenedor Spring
 * 
 * Conforme el framework crece en componentes obligatorios es necesario incluirlos en esta
 * enumeracion para automatizar sus pruebas de integracion
 * 
 * @author Ruben Cancino
 *
 */
public enum SWBeans {
	
	MessageSource
	,ResourceLocator
	,MenuFactory
	,ToolbarFactory
	,WindowPage
	,StatusBar
	,Header
	,ApplicationWindow
	,Application
	,ActionManager
	;
	
	public String toString(){
		return StringUtils.uncapitalize(name());
		
	}
}
