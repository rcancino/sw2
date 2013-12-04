package com.luxsoft.siipap.service;

import org.springframework.util.StringUtils;

/**
 * Enummeration of services provided by the {@link ServiceLocator2}
 * 
 * @author Ruben Cancino
 *
 */
public enum ServiceManagers {
	
	UniversalManager,
	UserManager,
	RoleManager,
	MailEngine,
	LookupManager
	;
	
	public String toString(){
		return StringUtils.uncapitalize(name());
	}

}
