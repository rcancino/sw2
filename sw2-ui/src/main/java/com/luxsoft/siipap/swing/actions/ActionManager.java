package com.luxsoft.siipap.swing.actions;

import javax.swing.Action;

public interface ActionManager extends ActionConfigurer{
	
	public Action getAction(String id);
	
	public Action registerAction(Action a,String id);
	
	//public Action getRoleBaseAction(String roleId);

}
