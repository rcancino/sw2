package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.gastos.ClasificacionDeActivo;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

/**
 * Forma para el mantenimiento de instancias de {@link ClasificacionDeActivo}
 * 
 * @author Ruben Cancino
 *
 */
public class ClasificacionDeActivoForm extends GenericAbstractForm<ClasificacionDeActivo>{
	

	public ClasificacionDeActivoForm(IFormModel model) {
		super(model);
		setTitle("Clasificación de Activo");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Tipo de Activo","Claisificación de Activo fijo");
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
		builder.append("Nombre",getControl("nombre"),5);
		builder.append("Descripción",getControl("descripcion"),5);
		builder.append("Tasa",getControl("tasa"),true);
		builder.append("Cuenta Contable",getControl("cuentaContable"));
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property) || "descripcion".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("cuentaContable".equals(property)){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(model.getModel(property), "AAA-AAAA-AAA");
			tf.setEnabled(!model.isReadOnly());
			return tf;
		}
		return null;
	}
	
	public static ClasificacionDeActivo showForm(ClasificacionDeActivo bean){
		return showForm(bean,false);
	}
	
	public static ClasificacionDeActivo showForm(ClasificacionDeActivo bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final ClasificacionDeActivoForm form=new ClasificacionDeActivoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (ClasificacionDeActivo)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new ClasificacionDeActivo());
		ClasificacionDeActivoForm.showObject(bean);
		System.exit(0);
	}

}
