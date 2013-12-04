package com.luxsoft.siipap.swing;

import javax.swing.JComponent;

/**
 * Small and simple interface that proiviedes a <code>JComponent</code> bind whith a unique id
 * The intent of this interface is that the application framework organizes its content in views
 * that can be controlled by a Page imlmenetation and this views could be implemented in small peaces
 * that could be easilly tested
 *  
 * Concrete implementations of this interface are resposable to implement the close method to
 * dispose resources used by the view in order to be ready for garbage collection
 *  
 * 
 * @author Ruben Cancino
 * 
 * TODO: Fire Events when appropiate
 */
public interface View {
	
	/**
	 * The id the uniquly identifies this view
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * The content of the
	 * 
	 * @return the GUI content of the view
	 */
	public JComponent getContent();
	
	/**
	 * Creates the content and opens the view.
	 *
	 */
	public void open();
	
	/**
	 * Should be used to close the view and dispose resources
	 * so the view can be garbage collected 
	 * 
	 * @return true if the view is closed
	 */
	public void close();
	
	/**
	 * Fired when the focus is gained
	 *
	 */
	public void focusGained();
	
	/**
	 * Fired when the focus is lost
	 *
	 */
	public void focusLost();
	
	
	/**
	 * Composition para delegar los aspectos visuales a un tercero
	 * 
	 * @return
	 */
	public VisualElement getVisualSupport();

	

}
