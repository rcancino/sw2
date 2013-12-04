package com.luxsoft.siipap.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * 
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractJavaBean implements Serializable {    

       
    
    protected transient PropertyChangeSupport support=new PropertyChangeSupport(this);
	
	public final synchronized void addPropertyChangeListener(
             PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
		 
	}
	 
	public final synchronized void removePropertyChangeListener(
             PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);		
	}
	
	public void firePropertyChange(String propertyName, Object old, Object newValue){
		support.firePropertyChange(propertyName, old, newValue);
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE,false);
	}
	
}
