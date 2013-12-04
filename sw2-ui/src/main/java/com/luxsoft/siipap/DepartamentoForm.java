package com.luxsoft.siipap;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;


/**
 * Forma para el mantenimiento de instancias de {@link Departamento}
 * 
 * @author Ruben Cancino
 *
 */
public class DepartamentoForm extends GenericAbstractForm<Departamento>{
	
	

	public DepartamentoForm(IFormModel model) {
		super(model);
		setTitle("Departamento");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Departamento","Mantenimiento de datos por Departamento");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Clave",getControl("clave"),true);		
		builder.append("Descripción",getControl("descripcion"),5);		
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("clave".equals(property)||"nombre".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}
		return null;
	}
	
	
	
	public static Departamento showForm(Departamento bean){
		return showForm(bean,false);
	}
	
	public static Departamento showForm(Departamento bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final DepartamentoForm form=new DepartamentoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Departamento)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new Departamento());
		DepartamentoForm.showObject(bean);
		System.exit(0);
	}

}
