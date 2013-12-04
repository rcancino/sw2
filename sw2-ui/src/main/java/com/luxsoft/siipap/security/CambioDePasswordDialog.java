package com.luxsoft.siipap.security;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class CambioDePasswordDialog extends AbstractForm{

	public CambioDePasswordDialog(IFormModel model) {
		super(model);
		
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout("","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		panel.add(builder.getPanel(),BorderLayout.CENTER);
		return panel;
	}
	
	

}
