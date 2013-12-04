package com.luxsoft.siipap.swing.matchers;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;

import org.apache.commons.beanutils.PropertyUtils;


import ca.odell.glazedlists.TextFilterator;



public class FechaSelector implements TextFilterator{
	
	private JFormattedTextField fechaSelector;
	private DateFormat format=new SimpleDateFormat("dd/MM/yyyy");
	private final String property;
	
	public FechaSelector(final String dateProperty){
		DateFormatter formatter=new DateFormatter(format);
		//format.setLenient(false);
		fechaSelector=new JFormattedTextField(formatter);
		
		this.property=dateProperty;
	}

	
	
	public JFormattedTextField getInputField(){
		return fechaSelector;
	}



	public void getFilterStrings(List baseList, Object element) {
		Date fecha;
		try {
			fecha = (Date)PropertyUtils.getProperty(element, property);
			baseList.add(format.format(fecha));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {			
			e.printStackTrace();
		}
		
	}
	
	

}
