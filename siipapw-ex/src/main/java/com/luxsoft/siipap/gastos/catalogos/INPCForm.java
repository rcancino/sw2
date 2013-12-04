package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

/**
 * Forma para el mantenimiento de instancias de {@link INPC}
 * 
 * @author Ruben Cancino
 *
 */
public class INPCForm extends GenericAbstractForm<INPC>{
	

	public INPCForm(IFormModel model) {
		super(model);
		setTitle("Catálogo de IPC");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("IPC","Indice nacional de precios al consumidor");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,70dlu, 2dlu," +
				"p,2dlu,70dlu","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}		
		builder.append("Año",addMandatory("year"));
		builder.append("Mes",getControl("mes"),true);		
		builder.append("Indice",addMandatory("indice"),true);
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("year".equals(property)){
			JComponent control=Binder.createYearBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("mes".equals(property)){
			JComponent control=Binder.createMesBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}
		return null;
	}
	
	public static INPC showForm(INPC bean){
		return showForm(bean,false);
	}
	
	public static INPC showForm(INPC bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final INPCForm form=new INPCForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (INPC)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new INPC());
		INPCForm.showObject(bean);
		System.exit(0);
	}

}
