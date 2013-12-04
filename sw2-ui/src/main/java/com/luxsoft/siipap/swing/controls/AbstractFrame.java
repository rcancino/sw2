package com.luxsoft.siipap.swing.controls;

import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.application.ResourceIDs;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.ScreenUtils;
import com.jgoodies.uif.util.WindowUtils;
import com.luxsoft.siipap.swing.ResourceLocator;

/**
 * Extension of JFrame for use as main window in framework
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractFrame extends JFrame{
	
	private final String windowID;
	private ResourceLocator resourceLocator;
	

	protected AbstractFrame(final String id){
		this.windowID=id;
	}
	
	public void build() {
		JComponent content = buildContentPane();
        
        // Work around the JRE's gray rect problem.
        // TODO: Remove this if fixed in the JRE.
        setBackground(content.getBackground());
        
		resize(content);
		setContentPane(content);
		pack();
		locateOnScreen();
		configureWindowIcon();		
		initEventHandling();
        configureCloseOperation();
	}
	
	/**
	 * Subclasses must override this method to build and return the content pane.
     * 
     * @return the built content pane
	 */
    protected  abstract JComponent buildContentPane();
    
    
    
    /**
     * Configures the behavior that will happen 
     * when the user initiates a "close" on this frame.<p>
     * 
     * 
     * @see JFrame#setDefaultCloseOperation(int)
     * @see Application#getApplicationCloseOnWindowClosingHandler()
     */
    protected void configureCloseOperation(){
    	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }
    
    /**
	 * Makes this frame visible. Allows subclasses to perform additional
     * operations immediately before or after the frame is made visible.
	 */
	public void open() {
		setVisible(true);
	}
	
	/**
	 * Template method to locate the frame on the screen
	 */
	protected void locateOnScreen() {
		ScreenUtils.locateOnScreenCenter(this);
	}
	
	/**
	 * Template method for custom resize of this frame
	 * 
	 * @param component
	 */	
	protected void resize(JComponent component) {
		Resizer.DEFAULT.resizeDialogContent(component);
	}
	
	/**
	 * Initialializes the event handling. The default implementation 
     * listens to frame resize events to ensure a minimum size and 
     * to keep track of the stored bounds.<p>
     * 
     * 
	 */	
	protected void initEventHandling() {
        addComponentListener(new WindowUtils.SizeChangeHandler(this));        
	}
	
	/**
     * Sets this frame Icon
     * 
     * @see WindowUtils#setImageIcon(java.awt.Frame, Image, Image)
     */
    protected void configureWindowIcon() {
    	if(getResourceLocator()==null)return;
        Image image12x12 = getResourceLocator().getImage(ResourceIDs.APPLICATION_ICON_12x12);
        Image image16x16 = getResourceLocator().getImage(ResourceIDs.APPLICATION_ICON_16x16);
        if (image16x16 == null) {
              image16x16 = getResourceLocator().getImage(ResourceIDs.APPLICATION_ICON);
        }
       	WindowUtils.setImageIcon(this, image12x12, image16x16);
    }

	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}
	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	public String getWindowID(){
		return this.windowID;
	}

}
