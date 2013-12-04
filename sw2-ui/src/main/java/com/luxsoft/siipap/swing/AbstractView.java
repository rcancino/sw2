package com.luxsoft.siipap.swing;

import java.awt.Image;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;

import com.luxsoft.siipap.swing.actions.ActionConfigurer;
import com.luxsoft.siipap.swing.actions.DefaultActionConfigurer;



/**
 * Basic implementation of View 
 * 
 * Stores itself as a client property in the  content <code>JComponent</code>  
 * 
 * @author Ruben Cancino
 *
 */
public abstract  class AbstractView  implements View,VisualElement,BeanNameAware{
	
	private String id;
	private VisualElementSupport visualSupport=new VisualElementSupport();
	protected JComponent content;
	protected Logger logger=Logger.getLogger(getClass());
	private ResourceLocator resourceLocator;
	private ActionConfigurer actionConfigurer;
	protected PropertyChangeSupport propertySupport=new PropertyChangeSupport(this);
	
	public AbstractView() {
	}
	
	
	public AbstractView(String id) {
		super();
		this.id = id;
	}

	public synchronized JComponent getContent() {
        if (content== null) {
            content = buildContent();            
        }
        return content;
    }
	
	/**
     * Builds and returns this view's panel. This method is called
     * by <code>#getPanel</code> if the panel has not been built before.
     * 
     * @return this view's panel
     */
    protected abstract JComponent buildContent();

	

    /**
     * Template method for subclases to do clean up work
     * like disposing resources for the garbage collector
     * 
     */
	public void close() {
		if(logger.isDebugEnabled()){
			logger.debug("Closing  and disposing view: "+getId());
		}
		try{
			dispose();
		}catch (Exception e) {
			String msg="Error en el metodo dispose de la vista {0} msg: {1}";
			logger.error(MessageFormat.format(msg, getId(),e.getMessage()));
			e.printStackTrace();
		}
	}
	
	/**
	 * Template method to dispose resources used by the view
	 *
	 */
	protected void dispose(){
		
	}

	/**
	 * Template method
	 */
	
	public void focusGained() {
		
	}
	
	public void focusLost(){
		
	}
	

	/**
	 * Template method 
	 */
	public void open() {
		if(logger.isDebugEnabled()){
			logger.debug("View opened: "+getId());
		}
	}
	
	protected String getString(String key){
		return getResourceLocator().getMessage(key);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public VisualElement getVisualSupport() {		
		return this.visualSupport;
	}
	
	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}
	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	public void setBeanName(String name) {
		setId(name);		
	}

	public ActionConfigurer getActionConfigurer() {
		if((actionConfigurer==null) && (getResourceLocator()!=null))
			actionConfigurer=new DefaultActionConfigurer(getResourceLocator());
		return actionConfigurer;
	}


	public void setActionConfigurer(ActionConfigurer actionConfigurer) {
		this.actionConfigurer = actionConfigurer;
	}    
	
	public Icon getIconFromResource(String path){
		try {
			ClassLoader cl=getClass().getClassLoader();
			URL url=cl.getResource(path);
			Icon icon=new ImageIcon(url);
			return icon;
		} catch (Exception e) {
			logger.info("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}


	public String getDescription() {
		return getVisualSupport().getDescription();
	}


	public Icon getIcon() {
		return getVisualSupport().getIcon();
	}


	public Image getImage() {
		return getVisualSupport().getImage();
	}


	public String getLabel() {
		return getVisualSupport().getLabel();
	}


	public String getTooltip() {
		return getVisualSupport().getTooltip();
	}


	public void setDescription(String description) {
		getVisualSupport().setDescription(description);
		
	}


	public void setIcon(Icon icon) {
		getVisualSupport().setIcon(icon);
	}


	public void setImage(Image image) {
		getVisualSupport().setImage(image);
	}


	public void setLabel(String label) {
		getVisualSupport().setLabel(label);
	}

	public void setTooltip(String tooltip) {
		getVisualSupport().setTooltip(tooltip);
	}


	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractView other = (AbstractView) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	
}
	
	