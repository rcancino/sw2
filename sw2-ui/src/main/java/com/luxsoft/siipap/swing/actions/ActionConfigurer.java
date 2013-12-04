package com.luxsoft.siipap.swing.actions;

import javax.swing.Action;

public interface ActionConfigurer {
	
	public static final String LABEL="label";	
	public static final char   MNEMONIC_MARKER   = '&';
	public static final String SHORT_DESCRIPTION = "tooltip";
	public static final String LONG_DESCRIPTION  = "description";
	public static final String SMALL_ICON		="icon";
	public static final String ACCELERATOR       = "accelerator";
	public static final String GRAY_ICON         = SMALL_ICON + ".gray";
	
	
	public void configure(Action action,String id);
	
	

}
