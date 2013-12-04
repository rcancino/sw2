package com.luxsoft.siipap.swing.binding;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.springframework.util.Assert;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Periodo;

/**
 * 
 * 
 * TODO Internacionalizar
 * 
 * @author Ruben Cancino
 *
 */
public class PeriodoPicker extends JPanel implements PropertyChangeListener{
	
	private Periodo periodo;
	
	private JXDatePicker fechaInicialPicker;
	
	private JXDatePicker fechaFinalPicker;
	
	public PeriodoPicker(){
		this(new Periodo());
	}
	
	public PeriodoPicker(final Periodo p){
		periodo=p;
		init();
	}
	
	private final void init(){		
		fechaInicialPicker=new JXDatePicker();
		fechaInicialPicker.setDate(periodo.getFechaInicial());
		fechaInicialPicker.setFormats(new String[]{"dd/MM/yyyy","dd/MM/yy"});		
		fechaFinalPicker=new JXDatePicker();
		fechaFinalPicker.setFormats(new String[]{"dd/MM/yyyy","dd/MM/yy"});
		fechaFinalPicker.setDate(periodo.getFechaFinal());
		fechaInicialPicker.getEditor().addPropertyChangeListener("value", this);
		fechaFinalPicker.getEditor().addPropertyChangeListener("value", this);
		setLayout(new BorderLayout());
		add(buildMainPanel(),BorderLayout.CENTER);		
	}
	
	private JComponent buildMainPanel(){
		FormLayout layout=new FormLayout("l:40dlu,3dlu,60dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append("Fecha Inicial", this.fechaInicialPicker,true);
		builder.append("Fecha Final", this.fechaFinalPicker);
		return builder.getPanel();		
	}
	
	

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		Assert.notNull(periodo);		
		Object old=this.periodo;
		this.periodo = periodo;
		firePropertyChange("periodo", old, periodo);
	}	
	
	
	private void actualizarPeriodo(){
		
		Date f1=fechaInicialPicker.getDate();
		Date f2=fechaFinalPicker.getDate();		
		
		Periodo pp=new Periodo(f1,f2);
		if(!pp.equals(periodo)){
			setPeriodo(pp);
		}
	}
	
	
	private Date cleanDate(final Date d){
		return DateUtils.truncate(d, Calendar.DATE);
		
	}


	public void propertyChange(PropertyChangeEvent evt) {
		
		if(evt.getSource()==fechaInicialPicker.getEditor()){
			//Valida que la fecha inicial sea inferior a la fecha final
			Date old=cleanDate((Date)evt.getOldValue());
			Date newVal=cleanDate((Date)evt.getNewValue());
			Date fechaFinal=cleanDate(fechaFinalPicker.getDate());
			long res=fechaFinal.getTime()-newVal.getTime();
			if(res>=0){
				//OK
				actualizarPeriodo();
			}else{
				//WRONG BEEP
				Toolkit.getDefaultToolkit().beep();				
				fechaInicialPicker.getEditor().removePropertyChangeListener("value", this);
				fechaInicialPicker.setDate(old);
				fechaInicialPicker.getEditor().addPropertyChangeListener("value",this);
			}
		}else{
			//Valida que la fecha final sea superior a la fecha inicial
			Date old=cleanDate((Date)evt.getOldValue());
			Date newVal=cleanDate((Date)evt.getNewValue());
			Date fechaInicial=cleanDate(fechaInicialPicker.getDate());
			long res=newVal.getTime()-fechaInicial.getTime();
			if(res>=0){
				//OK
				actualizarPeriodo();
			}else{
				Toolkit.getDefaultToolkit().beep();				
				fechaFinalPicker.getEditor().removePropertyChangeListener("value", this);
				fechaFinalPicker.setDate(old);
				fechaFinalPicker.getEditor().addPropertyChangeListener("value",this);
				
			}			
		}
	}	
	
	
	

}
