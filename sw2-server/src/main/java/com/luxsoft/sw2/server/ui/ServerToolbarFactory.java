package com.luxsoft.sw2.server.ui;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.swing.impl.ToolbarFactoryImpl;

public class ServerToolbarFactory extends ToolbarFactoryImpl{
	
	
	protected void addCustomButtons(ToolBarBuilder builder){
		builder.add(getActionManager().getAction("showLocalReplicationView"));
	//	builder.add(getActionManager().getAction(TesActions.ShowInventarioDeMaquilaView.getId()));
	}

}
