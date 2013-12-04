package com.luxsoft.siipap.cxp.ui.form;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class ContraRecibosFormDet extends AbstractForm{

	public ContraRecibosFormDet(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"50dlu,2dlu,f:70dlu, 3dlu, 50dlu,2dlu,f:70dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Documento");
		builder.append("Número", getControl("documento"));
		builder.append("Fecha", getControl("fecha"));
		builder.append("Tipo",getControl("tipo"));
		builder.append("Moneda",getControl("moneda"));
		builder.append("Total",getControl("total"));
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("tipo".equals(property)){
			SelectionInList sl=new SelectionInList(ContraReciboDet.Tipo.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}
	
	public static ContraReciboDet showForm(ContraReciboDet det){
		return showForm(det,false);
	}
	
	public static ContraReciboDet showForm(ContraReciboDet det,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(det,readOnly);
		ContraRecibosFormDet form=new ContraRecibosFormDet(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (ContraReciboDet)model.getBaseBean();
		}
		return null;
	}

	@Override
	protected JComponent buildHeader() {
	return new HeaderPanel("Recepción de documento","");
	}
	
	
	

}
