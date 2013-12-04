package com.luxsoft.sw2.server.ui;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.jgoodies.uif.builder.MenuBuilder;
import com.luxsoft.siipap.swing.impl.MenuFactoryImpl;

public class LocalServerMenuFactory extends MenuFactoryImpl{
	
	
	
	
	protected void buildCustomMenus(List<JMenu> customMenus){		
		
		customMenus.add(buildReplicacion());
		customMenus.add(buildConsultas());
	}
	
	protected JMenuBar buildMenuBar(){
		JMenuBar bar=super.buildMenuBar();		
		return bar;
	}	
	
	
	protected JMenu buildReplicacion(){
		MenuBuilder builder=new MenuBuilder("Servicios",'S');
		
		
		
		return builder.getMenu();
	}
	
	protected JMenu buildConsultas(){
		MenuBuilder builder=new MenuBuilder("Consultas",'C');
		builder.add(getActionManager().getAction("showLocalReplicationView"));
		builder.add(getActionManager().getAction("showReplicaView"));
		return builder.getMenu();
	}
	
	
}
