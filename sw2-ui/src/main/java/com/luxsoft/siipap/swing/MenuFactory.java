package com.luxsoft.siipap.swing;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 * Basic interface for the creation of Menus 
 *  
 * 
 * @author Ruben Cancino
 *
 */
public interface MenuFactory {
	
	public JMenuBar getMenuBar();
	
	/**
	 * Menu principal del modulo
	 * 
	 * @return
	 */
	public JMenu getModuleMenu();

}
