package com.luxsoft.siipap.swing;

import javax.swing.JFrame;

/**
 * Abstraccion del componenete principal para la interfaz grafica
 * 
 */
public interface ApplicationWindow {
	
	/**
	 * Abre la ventana principal 
	 *
	 */
	public void open();
	
	/**
	 * Cierra la ventana y los recursos que pudiera estar ocupando
	 *
	 */
	public void close();
	
	
	/**
	 * The frame to be displayed
	 * 
	 * @return
	 */
	public JFrame getWindow();
	
	/**
	 * Return the page that host the views
	 * 
	 * @return
	 */
	public Page getWindowPage();

}
