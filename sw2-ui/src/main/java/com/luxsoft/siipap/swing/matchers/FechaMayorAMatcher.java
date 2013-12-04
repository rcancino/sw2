package com.luxsoft.siipap.swing.matchers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;


public class FechaMayorAMatcher extends AbstractMatcherEditor implements ActionListener{
	
	protected final JFormattedTextField fechaField;
	
	public static String DATE_FORMAT="dd/MM/yyyy";
	
	protected String dateField;
	
	
	public FechaMayorAMatcher(){
		final SimpleDateFormat dateFormat=new SimpleDateFormat(DATE_FORMAT);
		final DateFormatter dateFormatter=new DateFormatter(dateFormat){
			public Object stringToValue(String text) throws ParseException {
				if(StringUtils.isBlank(text))
					return null;
				else 
					return super.stringToValue(text);
			}
		};
		dateFormat.setLenient(false);
		
		
		fechaField=new JFormattedTextField(dateFormatter);
		fechaField.addActionListener(this);
		
	}
	
	public String getDateField() {
		return dateField;
	}
	public void setDateField(String dateField) {
		this.dateField = dateField;
	}


	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {			
		Date fecha=(Date)fechaField.getValue();			
		if(fecha==null || StringUtils.isBlank(fechaField.getText())){
			fireMatchAll();
			//fechaInicialField.setValue(null);
		}
		else{
			if(StringUtils.isBlank(getDateField())){
				fireChanged(new MayorMatcher(fecha));
			}else
				fireChanged(new MayorMatcher(fecha,getDateField()));
			
		}
	}
	
	public Date getCurrentDate(){
		Date fecha=(Date)fechaField.getValue();			
		return fecha;
	}
	
	public void setDate(final Date date){
		fechaField.setValue(date);
	}
	
	protected boolean comparaFecha(final Date fecha,final Date fechaFromBean){
		return fecha.compareTo(fechaFromBean)<=0;
	}
	
	protected  class MayorMatcher implements Matcher{
		
		private final Date fecha;
		private String dateProperty;
		
		public MayorMatcher(final Date fecha){
			this(fecha,"fecha");
		}
		
		public MayorMatcher(final Date fecha,final String dateProperty){
			this.fecha=fecha;
			this.dateProperty=dateProperty;
		}

		public boolean matches(Object item) {	
			final Date dateFromBean=getPropertyFromBean(item);
			if(dateFromBean==null)
				return true;
			return comparaFecha(fecha, dateFromBean);
		}
		
		private Date getPropertyFromBean(final Object bean){
			try {
				Object val=PropertyUtils.getProperty(bean, dateProperty);
				Date date=(Date)val;
				return DateUtils.round(date,Calendar.DATE);
			} catch (Exception  e) {
				//e.printStackTrace();
				return null;
			}
			
		}
		
	}

	public JFormattedTextField getFechaField() {
		return fechaField;
	}


}
