package com.luxsoft.siipap.pos.ui.forms;

import java.awt.BorderLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

public class RegistroRecepcionClienteForm extends SXAbstractDialog{
	
	private final boolean enabled;
	
	public RegistroRecepcionClienteForm() {
		super("Hora de salida");
		this.enabled=true;
		
	}
	
	public JComponent buildHeader(){
		return new HeaderPanel("Hora de llegada con el cliente"
				,"Posteriormente  este dato no se puede modificar");
	}
	
	private JSpinner salidaField;
	@Override
	protected JComponent buildContent() {
		salidaField=new JSpinner();
		SpinnerDateModel model=new SpinnerDateModel(new Date(),null,null,Calendar.MINUTE);
		salidaField.setModel(model);
		salidaField.setEnabled(enabled);
		afterCreated(salidaField);
		JPanel panel=new JPanel(new BorderLayout());
		FormLayout layout=new FormLayout("40dlu,2dlu,100dlu","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Salida:",salidaField);
		panel.add(builder.getPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}

	protected void afterCreated(JSpinner salidaField){
		//salidaField.setEnabled(false);
	}

	public static Date seleccionar(boolean enabled){
		RegistroRecepcionClienteForm form=new RegistroRecepcionClienteForm();
		form.open();
		if(!form.hasBeenCanceled()){
			return (Date)form.salidaField.getValue();
		}
		return null;
	}
	public static Date seleccionar(){
		return seleccionar(false);
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				seleccionar();
				System.exit(0);
			}

		});
	}

	
}
