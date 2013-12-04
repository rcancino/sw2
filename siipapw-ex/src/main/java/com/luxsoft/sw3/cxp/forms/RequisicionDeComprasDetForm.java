/**
 * 
 */
package com.luxsoft.sw3.cxp.forms;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.CantidadMonetariaControl;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class RequisicionDeComprasDetForm extends GenericAbstractForm<RequisicionDe>{

	public RequisicionDeComprasDetForm(IFormModel model) {
		super(model);
		setTitle("Cuenta por pagar");
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,4dlu,f:p:g, 2dlu," +
				"p,4dlu,70dlu" 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Documento", addReadOnly("documento"),5);
		builder.append("Fecha",addReadOnly("fechaDocumento"),5);
		builder.append("Total",getControl("total"),5);			
		builder.append("Comentario",getControl("comentario"),5);
					
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("total".equals(property)){
			CantidadMonetariaControl c=new CantidadMonetariaControl(buffer(model.getModel(property)));
			c.getMonedaBox().setEnabled(false);
			c.getInputField().setEditable(!model.isReadOnly());
			return c;
		}else if("comentario".equals(property)){
			JComponent c=Binder.createMayusculasTextField(buffer(model.getModel(property)));
			c.setEnabled(!model.isReadOnly());
			return c;
		}
		return null;
	}
	
	
	
	
}