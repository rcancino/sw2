package com.luxsoft.siipap.swing.utils;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.properties.TabWindowProperties;
import net.infonode.docking.theme.ClassicDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.util.Direction;

public class DockingUtils {
	
	public static void configRootWindow(RootWindow rootWindow){
		
		final RootWindowProperties properties = new RootWindowProperties();
		final DockingWindowsTheme theme=new ClassicDockingTheme();
		final RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();
		
		properties.addSuperObject(titleBarStyleProperties);
		
		properties.addSuperObject(theme.getRootWindowProperties());
		
		//rootWindow.setPopupMenuFactory(WindowMenuUtil.createWindowMenuFactory(viewMap, true));
		//Our properties object is the super object of the root window properties object, so all property values of the
	    // theme and in our property object will be used by the root window
	    
		rootWindow.getRootWindowProperties().addSuperObject(properties);
		//fixLookOfRootWindow(rootWindow);		
		rootWindow.getWindowBar(Direction.UP).setEnabled(true);
		rootWindow.setPreferredMinimizeDirection(Direction.UP);
		rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
		//rootWindow.setPreferredSize(getPreferedSize());
		
		//configTabWindowProperties();
				
	}
	
	public static void configTabWindowProperties(RootWindow rootWindow){
		TabWindowProperties props=new TabWindowProperties();
		props.getMaximizeButtonProperties().setVisible(false);
		props.getMinimizeButtonProperties().setVisible(false);
		props.getRestoreButtonProperties().setVisible(false);
		props.getCloseButtonProperties().setVisible(false);
		
		props.getTabbedPanelProperties().setPaintTabAreaShadow(false);
		props.getTabbedPanelProperties().setShadowEnabled(false);
		rootWindow.getRootWindowProperties().getTabWindowProperties().addSuperObject(props);
	}
	
	public static void configTabWindowForFilter(View view){
		TabWindowProperties props=new TabWindowProperties();
		props.getMaximizeButtonProperties().setVisible(false);
		props.getMinimizeButtonProperties().setVisible(false);
		props.getRestoreButtonProperties().setVisible(false);
		props.getCloseButtonProperties().setVisible(false);
		
		props.getTabbedPanelProperties().setPaintTabAreaShadow(false);
		props.getTabbedPanelProperties().setShadowEnabled(false);
		view.getRootWindow()
			.getRootWindowProperties()
			.getTabWindowProperties()
			.addSuperObject(props);
	}

}
