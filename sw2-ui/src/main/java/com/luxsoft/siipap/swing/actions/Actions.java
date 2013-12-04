package com.luxsoft.siipap.swing.actions;

import org.springframework.util.StringUtils;

/**
 * Enumeracion de acciones generales y globales al Framework
 * 
 * @author Ruben Cancino
 *
 */
public enum Actions {
	
	ExitApplication
	,ShowWelcomeView
	;
	
	public String toString(){
		return StringUtils.uncapitalize(name());
		
	}

}
