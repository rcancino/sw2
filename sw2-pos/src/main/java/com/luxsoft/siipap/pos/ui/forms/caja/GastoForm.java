package com.luxsoft.siipap.pos.ui.forms.caja;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class GastoForm extends AbstractForm{

	public GastoForm(IFormModel model) {
		super(model);
		setTitle("Registro de gasto");
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout("p,2dlu,max(p;100dlu),3dlu,p,2dlu,max(p;100dlu):g(.5)","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Descripción",getControl("descripcion"),5);
		builder.append("Factura",getControl("documento"));
		builder.append("Importe",getControl("importe"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"));
		return builder.getPanel();
	}
	

}
