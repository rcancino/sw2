package com.luxsoft.siipap.swing;

import java.util.List;

import javax.swing.JComponent;

/**
 * 
 * 
 * @author Ruben Cancino
 *
 */
public interface Page {
	
	/**
	 * Regresa el contenedor que hospeda las vistas 
	 * 
	 * @return
	 */
	public JComponent getContainer();
	
		
	/**
	 * Agrega una vista al contenedor
	 * 
	 * @param view
	 */
	public void addView(final View view);
	
	
	/**
	 * Cierra todas y cada una de las vistas hospedadas detonando los metodos lostFocus y close 
	 * de cada una de ellas
	 *
	 */
	public void close();
	
	
	/**
	 * Regresa una lista de las vistas hospedadas
	 * 
	 * @return
	 */
	public List<View> getViews();
	
	
	

}
