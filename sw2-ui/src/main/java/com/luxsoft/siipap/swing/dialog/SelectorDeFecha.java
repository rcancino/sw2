package com.luxsoft.siipap.swing.dialog;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

public class SelectorDeFecha extends SXAbstractDialog{
	
	private Date defaultDate=null;

	public SelectorDeFecha() {
		super("Seleccion de fecha");
	}
	
	public SelectorDeFecha(final Date date) {
		super("Seleccion de fecha");
		this.defaultDate=date;
	}
	
	private JXDatePicker fechaControl;

	@Override
	protected JComponent buildContent() {
		JPanel panel=new JPanel(new BorderLayout());
		
		fechaControl=new JXDatePicker();
		if(defaultDate!=null)
			fechaControl.setDate(defaultDate);
		fechaControl.setFormats("dd/MM/yyyy");
		FormLayout layout=new FormLayout("p,2dlu,60dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Fecha",fechaControl);
		panel.add(builder.getPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}
	
	public Date getFecha(){
		return fechaControl.getDate();
	}
	
	public static Date seleccionar(String titulo){
		SelectorDeFecha selector=new SelectorDeFecha();
		selector.setTitle(titulo);
		selector.open();
		
		if(!selector.hasBeenCanceled()){
			return selector.getFecha();
		}
		return null;
	}
	
	public static Date seleccionar(){
		SelectorDeFecha selector=new SelectorDeFecha();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getFecha();
		}
		return null;
	}
	
	public static Date seleccionar(final Date date){
		SelectorDeFecha selector=new SelectorDeFecha(date);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getFecha();
		}
		return null;
	}

}
