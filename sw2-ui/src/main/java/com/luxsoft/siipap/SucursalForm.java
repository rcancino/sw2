package com.luxsoft.siipap;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.Sucursal;

import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;


/**
 * Forma para el mantenimiento de instancias de {@link Sucursal}
 * 
 * @author Ruben Cancino
 *
 */
public class SucursalForm extends GenericAbstractForm<Sucursal>{
	
	

	public SucursalForm(IFormModel model) {
		super(model);
		setTitle("Sucursal");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Sucursal","Mantenimiento a sucursal de la empresa");
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
		builder.append("Empresa",getControl("empresa"),5);
		builder.append("Clave",getControl("clave"),true);
		builder.append("Nombre",getControl("nombre"),5);
		builder.nextLine();
		builder.append("Dirección",getControl("direccion"),5);
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}else if("empresa".equals(property)){
			JComboBox box= Bindings.createEmpresaBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return null;
	}
	
	
	
	public static Sucursal showForm(Sucursal bean){
		return showForm(bean,false);
	}
	
	public static Sucursal showForm(Sucursal bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final SucursalForm form=new SucursalForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Sucursal)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new Sucursal());
		SucursalForm.showObject(bean);
		System.exit(0);
	}

}
