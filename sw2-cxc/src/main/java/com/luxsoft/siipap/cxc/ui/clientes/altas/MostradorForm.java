/**
 * 
 */
package com.luxsoft.siipap.cxc.ui.clientes.altas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

public class MostradorForm extends AbstractForm implements PropertyChangeListener{

	public MostradorForm(IFormModel model) {
		super(model);
		setTitle("Cliente mostrador");
		model.addBeanPropertyChangeListener(this);
		
	}
	
	public void close(){
		model.removeBeanPropertyChangeListener(this);
		super.close();
	}

	@Override
	protected JComponent buildFormPanel() {			
		final FormLayout layout=new FormLayout("p,2dlu,250dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		builder.append("Apellido Paterno",getControl("apellidoP"));
		builder.append("Apellido Materno",getControl("apellidoM"));			
		builder.append("Nombre (s)",getControl("nombres"));
		builder.append("Persona Física",getControl("personaFisica"));
		builder.append("Nombre",getControl("nombre"));
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		return builder.getPanel();
	}
	/*
	public void open(){
		getControl("apellidoP").setFocusCycleRoot(true);
		super.open();
		
	}
*/
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property) || "nombres".equals(property) || "apellidoP".equals(property) || "apellidoM".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
		}
		return super.createCustomComponent(property);
	}

	public void propertyChange(PropertyChangeEvent evt) {			
		if("personaFisica".equals(evt.getPropertyName())){
			Boolean pf=(Boolean)evt.getNewValue();
			getControl("nombre").setEnabled(!pf);
			getControl("apellidoP").setEnabled(pf);
			getControl("apellidoM").setEnabled(pf);
			getControl("nombres").setEnabled(pf);
		}			
	}
	
}