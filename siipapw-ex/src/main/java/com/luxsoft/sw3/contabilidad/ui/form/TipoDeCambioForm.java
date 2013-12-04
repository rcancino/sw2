package com.luxsoft.sw3.contabilidad.ui.form;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class TipoDeCambioForm extends AbstractForm{

	public TipoDeCambioForm(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,3dlu,p","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Fecha",getControl("fecha"));
		builder.append("T.C.",getControl("factor"));
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("factor".equals(property)){
			JComponent c=Binder.createNumberBinding(model.getModel(property), 4);
			return c;
		}
		return super.createCustomComponent(property);
	}
	
	
}
