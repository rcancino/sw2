package com.luxsoft.siipap.swing.matchers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import org.jdesktop.swingx.JXDatePicker;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

/**
 * Matcher editor para filtrar listas por alguna fecha
 * 
 * 
 * 
 * @author Ruben Cancino
 *
 */
public abstract class FechaMatcherEditor extends AbstractMatcherEditor implements PropertyChangeListener{
	
	protected JXDatePicker picker;
	
	
	public FechaMatcherEditor(){
		picker=new JXDatePicker();		
		picker.setFormats(new String[]{"dd/MM/yy"});
		picker.getEditor().setText("");
		picker.addPropertyChangeListener(this);
		
	}

	public void propertyChange(PropertyChangeEvent evt) {		
		if("date".equalsIgnoreCase(evt.getPropertyName())){
			Object val=evt.getNewValue();
			if(val==null)
				fireMatchAll();
			else{
				Date fecha=(Date)val;
				evaluarFecha(fecha);
			}
		}
	}
	
	protected abstract void evaluarFecha(final Date fecha);
	
	public JXDatePicker getPikcer(){
		return picker;
	}
	
	

}
